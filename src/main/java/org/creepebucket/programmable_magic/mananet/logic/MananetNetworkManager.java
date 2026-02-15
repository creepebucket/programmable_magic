package org.creepebucket.programmable_magic.mananet.logic;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

import java.util.*;
import java.util.function.Supplier;

/**
 * Mananet 的运行时管理器（按 {@link ServerLevel} 维度）。
 *
 * <p>职责：</p>
 * <ul>
 *     <li>维护网络 union-find（{@code parent}）与每个网络的汇总状态（availableMana/cache/load/size）。</li>
 *     <li>缓存“节点方块”的按位置状态（{@code blockNodes}）。</li>
 *     <li>提供多个延迟队列（dirty/edgeChange/removal），让结构更新在 tick 中统一处理。</li>
 * </ul>
 *
 * <p>注意：该类只负责“数据与队列”，具体的合并/拆分逻辑在 {@code MananetNetworkLogic}。</p>
 */
public final class MananetNetworkManager {

    /**
     * 每个服务器维度一个 manager（不跨维度共享网络）。
     */
    private static final Map<ServerLevel, MananetNetworkManager> MANAGERS = new HashMap<>();
    /**
     * 网络根 id -> 汇总状态。
     */
    private final Map<UUID, NetworkState> networks = new HashMap<>();
    /**
     * 节点方块位置 -> 节点状态。
     *
     * <p>key 使用 {@code BlockPos.asLong()}，便于作为 fastutil 的 long map 使用。</p>
     */
    private final Long2ObjectOpenHashMap<MananetNodeState> blockNodes = new Long2ObjectOpenHashMap<>();
    /**
     * union-find 的 parent 指针：children -> parent。
     */
    private final Map<UUID, UUID> parent = new HashMap<>();
    /**
     * 节点需要 integrate 的位置队列。
     *
     * <p>用 set + queue 做去重：queue 负责顺序，set 负责“一次 tick 内不重复”。</p>
     */
    private final ArrayDeque<Long> dirtyQueue = new ArrayDeque<>();
    private final HashSet<Long> dirtySet = new HashSet<>();
    private final ArrayDeque<EdgeChange> edgeChangeQueue = new ArrayDeque<>();
    private final ArrayDeque<NodeRemoval> removalQueue = new ArrayDeque<>();
    private final HashSet<Long> removalSet = new HashSet<>();
    /**
     * 标记持久化层（SavedData 中的 parent）是否已装载到运行时。
     */
    private boolean persistentLoaded = false;

    public static MananetNetworkManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, ignored -> new MananetNetworkManager());
    }

    public NetworkState getOrCreate(UUID id) {
        return networks.computeIfAbsent(id, NetworkState::new);
    }

    /**
     * 查找网络根 id，并做路径压缩。
     */
    public UUID resolveNetworkId(UUID id) {
        // null 直接透传：上层用 null 表示“未接网”。
        if (id == null) return null;
        // parent 中没有记录：说明该 id 自身就是根。
        UUID p = parent.get(id);
        if (p == null) return id;
        // 递归找到根，并做路径压缩（把当前 id 直接挂到根上）。
        UUID root = resolveNetworkId(p);
        if (!root.equals(p)) parent.put(id, root);
        return root;
    }

    public boolean isPersistentLoaded() {
        return persistentLoaded;
    }

    /**
     * 从持久化层装载 parent 表（只应在首次 chunk load 时调用一次）。
     */
    public void loadPersistentParent(Map<UUID, UUID> persistedParent) {
        parent.clear();
        parent.putAll(persistedParent);
        persistentLoaded = true;
    }

    /**
     * 导出当前 union-find 的 parent 表，用于写入 SavedData。
     */
    public Map<UUID, UUID> exportParent() {
        return new HashMap<>(parent);
    }

    /**
     * 合并两个网络（按 size 做启发式合并），并合并汇总数据。
     *
     * @return 合并后的根 id
     */
    public UUID union(UUID a, UUID b) {
        // 先把 a/b 都规范化为根 id（union-find 的基本前置）。
        UUID ra = resolveNetworkId(a);
        UUID rb = resolveNetworkId(b);
        // 已在同一根：无需合并。
        if (ra.equals(rb)) return ra;

        // 取出两侧网络汇总状态（不存在则创建）。
        NetworkState sa = networks.get(ra);
        NetworkState sb = networks.get(rb);
        if (sa == null) sa = getOrCreate(ra);
        if (sb == null) sb = getOrCreate(rb);

        // 按 size 启发式合并：尽量把小网络挂到大网络下面，降低后续 find 的路径长度。
        if (sa.size < sb.size) {
            UUID tmp = ra;
            ra = rb;
            rb = tmp;
            NetworkState ts = sa;
            sa = sb;
            sb = ts;
        }

        // union-find：把 rb 的 parent 指向 ra（ra 成为新根）。
        parent.put(rb, ra);
        // 合并汇总数据：availableMana/cache/load/size 都按加法聚合。
        sa.mana.add(sb.mana);
        sa.cache.add(sb.cache);
        sa.load.add(sb.load);
        sa.size += sb.size;
        // rb 不再是根：从 networks map 中移除其汇总状态。
        networks.remove(rb);
        // 合并后把 availableMana 夹紧到 [0, cache]，保持网络状态合法。
        sa.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(sa.mana), sa.cache);
        return ra;
    }

    /**
     * 获取或创建某个位置的节点状态（仅用于节点方块）。
     *
     * <p>init supplier 通常会调用 {@code AbstractNodeBlock.init_node_state} 填充默认 cache/load。</p>
     */
    public MananetNodeState getOrCreateBlockNode(BlockPos pos, Supplier<MananetNodeState> init) {
        return blockNodes.computeIfAbsent(pos.asLong(), ignored -> init.get());
    }

    public MananetNodeState getBlockNode(BlockPos pos) {
        return blockNodes.get(pos.asLong());
    }

    public MananetNodeState getBlockNode(long posLong) {
        return blockNodes.get(posLong);
    }

    public void putBlockNode(long posLong, MananetNodeState state) {
        blockNodes.put(posLong, state);
    }

    public void removeBlockNode(long posLong) {
        blockNodes.remove(posLong);
    }

    public void removeBlockNode(BlockPos pos) {
        blockNodes.remove(pos.asLong());
    }

    public Iterable<Long2ObjectMap.Entry<MananetNodeState>> iterateBlockNodes() {
        return blockNodes.long2ObjectEntrySet();
    }

    /**
     * 获取网络状态（若 id 不存在则返回 null）。
     */
    public NetworkState getNetworkIfPresent(UUID id) {
        return networks.get(resolveNetworkId(id));
    }

    public Iterable<NetworkState> iterateNetworks() {
        return networks.values();
    }

    /**
     * 标记某位置需要 integrate。
     */
    public void markDirty(BlockPos pos) {
        long key = pos.asLong();
        // 用 dirtySet 去重：同一位置在队列里最多出现一次。
        if (dirtySet.add(key)) dirtyQueue.addLast(key);
    }

    public Long pollDirty() {
        while (true) {
            Long key = dirtyQueue.pollFirst();
            if (key == null) return null;
            // 弹出时再用 dirtySet 确认有效（处理重复入队或过期项）。
            if (dirtySet.remove(key)) return key;
        }
    }

    public void enqueueEdgeChange(BlockPos pos, Direction dir, boolean oldValue, boolean newValue) {
        // 连边变化不在此处直接处理：只入队，交给逻辑层在 tick 中统一消费。
        edgeChangeQueue.addLast(new EdgeChange(pos.asLong(), dir, oldValue, newValue));
    }

    public EdgeChange pollEdgeChange() {
        return edgeChangeQueue.pollFirst();
    }

    /**
     * 入队节点移除事件。
     *
     * <p>同一位置只保留一次（用 removalSet 去重）。</p>
     */
    public void enqueueRemoval(BlockPos pos, UUID oldNetworkId, int connectivityMask, Mana cache, Mana load, int connectedNeighbors) {
        long key = pos.asLong();
        // 同一位置只允许入队一次：避免重复拆分/重复扣除贡献。
        if (!removalSet.add(key)) return;
        removalQueue.addLast(new NodeRemoval(key, oldNetworkId, connectivityMask, cache, load, connectedNeighbors));
    }

    public NodeRemoval pollRemoval() {
        NodeRemoval removal = removalQueue.pollFirst();
        if (removal == null) return null;
        // 出队时同步移除去重标记，允许未来再次为同一位置入队新事件。
        removalSet.remove(removal.posLong);
        return removal;
    }

    public int getSize(UUID id) {
        UUID root = resolveNetworkId(id);
        return networks.get(root) != null ? networks.get(root).size : 0;
    }

    /**
     * 读取网络当前魔力快照。
     *
     * <p>这里返回 copy，避免调用方意外修改内部对象。</p>
     */
    public Mana getMana(UUID id) {
        UUID root = resolveNetworkId(id);
        return ManaMath.copy(getOrCreate(root).mana);
    }

    /**
     * 获取网络汇总信息快照（用于调试/展示）。
     */
    public NetworkInfo getNetworkInfo(UUID id) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        return new NetworkInfo(root, ManaMath.copy(state.mana), ManaMath.copy(state.cache), ManaMath.copy(state.load), state.size);
    }

    public void setNetwork(UUID id, Mana mana, Mana cache, Mana load, int size) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        // 约束：写入的 availableMana 会先做非负化与 cache 夹紧，防止网络存量越界。
        state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(mana), cache);
        state.cache = cache;
        state.load = load;
        state.size = size;
    }

    public void removeNetwork(UUID id) {
        UUID root = resolveNetworkId(id);
        networks.remove(root);
        parent.remove(root);
    }

    public void removeNetworks(Iterable<UUID> ids) {
        for (UUID id : ids) removeNetwork(id);
    }

    public void addMana(UUID id, Mana delta) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        // 对网络存量做增量修改，并保持 [0, cache] 约束。
        state.mana.add(delta);
        state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(state.mana), state.cache);
    }

    /**
     * 是否能支付本网络的“消耗部分”。
     *
     * <p>load 为负（产出）不会产生成本，因此这里仅检查每 tick 负载的正部。</p>
     */
    public boolean canProduce(UUID id) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        Mana perTick = ManaMath.scale(state.load, 1.0 / 20.0);
        Mana cost = ManaMath.positivePart(perTick);
        return ManaMath.canAfford(state.mana, cost);
    }

    public void applyContribution(UUID networkId, Mana cacheDelta, Mana loadDelta, int sizeDelta) {
        UUID root = resolveNetworkId(networkId);
        NetworkState state = getOrCreate(root);
        // 节点贡献变更：cache/load/size 按增量聚合到网络汇总中。
        state.cache.add(cacheDelta);
        state.load.add(loadDelta);
        state.size += sizeDelta;
        // 贡献变化后，当前 availableMana 也需要重新夹紧到新 cache（例如 cache 下降的场景）。
        state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(state.mana), state.cache);
    }

    /**
     * 推进网络状态一个 tick：按 load 积分到 availableMana。
     *
     * <p>约定：load 为每秒变化量，因此每 tick 变化量为 {@code load / 20}。</p>
     */
    public void tick() {
        for (NetworkState state : networks.values()) {
            // load 约定为“每秒净变化量”，因此每 tick 的变化量是 load / 20。
            Mana perTick = ManaMath.scale(state.load, 1.0 / 20.0);
            // 消耗成本只看正部：负部代表产出，不需要先支付。
            Mana cost = ManaMath.positivePart(perTick);
            // 能支付则执行本 tick 的净变化（负 load 会增加 availableMana，正 load 会减少 availableMana）。
            if (ManaMath.canAfford(state.mana, cost)) state.mana.add(ManaMath.scale(state.load, -1.0 / 20.0));
            // 每 tick 都收敛一次，保证网络状态始终在合法范围内。
            state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(state.mana), state.cache);
        }
    }

    /**
     * 单个网络的汇总状态（按网络根 id 索引）。
     *
     * <p>其中：</p>
     * <ul>
     *     <li>{@code cache/load/size} 是节点贡献的求和。</li>
     *     <li>{@code availableMana} 是当前存量，会被夹紧到 {@code cache}。</li>
     * </ul>
     */
    public static final class NetworkState {
        public final UUID id;
        public Mana mana = new Mana();
        public Mana cache = new Mana();
        public Mana load = new Mana();
        public int size = 0;

        private NetworkState(UUID id) {
            this.id = id;
        }
    }

    /**
     * 连边开关变化事件（由节点侧写入，逻辑层在 tick 中消费）。
     */
    public record EdgeChange(long posLong, Direction dir, boolean oldValue, boolean newValue) {
    }

    /**
     * 节点移除事件（包含被移除节点当时的贡献与连通信息）。
     *
     * <p>之所以把 cache/load/connectivityMask 打包进事件，是为了在方块已消失后仍能正确扣除贡献/判断是否需要拆分。</p>
     */
    public record NodeRemoval(long posLong, UUID oldNetworkId, int connectivityMask, Mana cache, Mana load,
                              int connectedNeighbors) {
    }

    /**
     * 对外暴露的网络信息快照（用于调试/展示）。
     */
    public record NetworkInfo(UUID id, Mana mana, Mana cache, Mana load, int size) {
    }
}
