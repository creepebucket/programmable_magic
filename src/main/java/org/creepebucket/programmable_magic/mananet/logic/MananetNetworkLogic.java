package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;
import org.creepebucket.programmable_magic.mananet.api.MananetNode;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Mananet 的核心网络逻辑（结构更新）。
 *
 * <p>整体策略是“延迟处理”：放置/移除/连通开关等事件只入队，真正的网络合并/拆分在
 * {@link #processPending(ServerLevel)} 中统一处理，避免在事件回调里递归更新邻居。</p>
 *
 * <h3>节点来源</h3>
 * <ul>
 *     <li>方块实体节点：方块实体直接实现 {@link MananetNode}。</li>
 *     <li>节点方块：继承 {@link AbstractNodeBlock}，由 {@link MananetBlockNode} 做按位置封装。</li>
 * </ul>
 *
 * <h3>网络 id</h3>
 * <ul>
 *     <li>网络使用 UUID 标识。</li>
 *     <li>合并使用 union-find（见 {@code MananetNetworkManager.parent}）。</li>
 *     <li>节点状态里保存的 {@code networkId} 视为“归属”，逻辑层会把它规范化为根 id。</li>
 * </ul>
 */
public final class MananetNetworkLogic {

    private MananetNetworkLogic() {}

    /**
     * 获取节点访问器（不做 integrate 的副作用）。
     *
     * <p>如果你期望确保节点已分配 network_id，请使用 {@code MananetNodes.get(level, pos)}。</p>
     */
    public static MananetNode getNodeAccess(ServerLevel level, BlockPos pos) {
        return getNodeOrBlockNode(level, pos);
    }

    /**
     * 标记某位置需要 integrate。
     */
    public static void markDirty(ServerLevel level, BlockPos pos) {
        MananetNetworkManager.get(level).markDirty(pos);
    }

    /**
     * 入队“连边开关变化”事件。
     *
     * <p>注意：这里不要求两端都已连通；实际连接建立/断开会在消费事件时再判断。</p>
     */
    public static void enqueueConnectivityChange(ServerLevel level, BlockPos pos, Direction dir, boolean oldValue, boolean newValue) {
        MananetNetworkManager.get(level).enqueueEdgeChange(pos, dir, oldValue, newValue);
    }

    /**
     * 入队“节点移除”事件（不携带 cache/load 贡献，默认当作 0）。
     *
     * <p>当前主要用于“按状态拆分”路径之外的简单移除场景。</p>
     */
    public static void enqueueRemoval(ServerLevel level, BlockPos pos, UUID oldNetworkId, int connectivityMask) {
        MananetNetworkManager.get(level).enqueueRemoval(pos, oldNetworkId, connectivityMask, new Mana(), new Mana(), 0);
    }

    /**
     * 节点方块被移除时调用：清理该位置的持久化与运行时状态，并把“可能需要拆分”的信息入队。
     *
     * <p>这里会计算 {@code connectedNeighbors}：被移除节点在旧网络内、且与其双向连通的邻居数量。
     * 当该数量大于 1 时，移除有可能把网络拆成多个连通分量，需要额外的拆分逻辑。</p>
     */
    public static void enqueueBlockRemoval(ServerLevel level, BlockPos pos, AbstractNodeBlock block) {
        // 取得该维度的运行时管理器（网络状态、节点状态、待处理队列都在这里）。
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        // 读取该位置的节点状态快照（用于扣除贡献、判断是否需要拆分）。
        MananetNodeState state = manager.getBlockNode(pos);
        // 先从 chunk 附件中移除持久化记录，避免卸载/保存时残留该节点数据。
        MananetNetworkPersistence.removeNode(level, pos);
        // 再从运行时缓存移除该位置节点状态（此时该位置已不再被视为网络中的节点）。
        manager.removeBlockNode(pos);
        // 没有状态或未加入任何网络：不需要触发拆分路径。
        if (state == null || state.networkId == null) return;
        // 规范化到网络根 id：后续都以 root 为分组/对比基准。
        UUID root = manager.resolveNetworkId(state.networkId);
        int connected = 0;
        // 统计：被移除节点在旧网络内、且“双方连通”的邻居数量（>1 时移除可能造成网络拆分）。
        for (Direction dir : Direction.values()) {
            int bit = 1 << dir.ordinal();
            // 本端该方向未开边：该方向不可能形成连通。
            if ((state.connectivityMask & bit) == 0) continue;
            BlockPos np = pos.relative(dir);
            // 邻居必须也是节点方块（且有节点状态）；否则该方向不计入连通邻居。
            MananetNodeState ns = getOrCreateNodeState(level, np);
            if (ns == null || ns.networkId == null) continue;
            // 邻居必须属于同一旧网络（对比 root）。
            if (!manager.resolveNetworkId(ns.networkId).equals(root)) continue;
            int obit = 1 << dir.getOpposite().ordinal();
            // 邻居端反向未开边：不算双向连通。
            if ((ns.connectivityMask & obit) == 0) continue;
            connected++;
        }
        // 把“节点移除事件”入队，由 tick 统一消费并进行扣贡献/拆分处理。
        manager.enqueueRemoval(pos, root, state.connectivityMask, state.cache, state.load, connected);
    }

    /**
     * 在 tick 中处理所有待处理事件：
     * <ul>
     *     <li>节点移除（可能触发网络拆分）</li>
     *     <li>连边变化（可能触发合并/拆分）</li>
     *     <li>dirty integrate（为未分配 network_id 的节点分配并合并邻接网络）</li>
     * </ul>
     */
    public static void processPending(ServerLevel level) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);

        // 1) 消费节点移除队列：按旧网络根 id 分组，确保拆分逻辑在“网络维度”上成批处理。
        java.util.HashMap<UUID, java.util.ArrayList<MananetNetworkManager.NodeRemoval>> removalGroups = new java.util.HashMap<>();
        while (true) {
            MananetNetworkManager.NodeRemoval removal = manager.pollRemoval();
            if (removal == null) break;
            UUID root = manager.resolveNetworkId(removal.oldNetworkId());
            removalGroups.computeIfAbsent(root, ignored -> new java.util.ArrayList<>()).add(removal);
        }
        for (var entry : removalGroups.entrySet()) processRemovalGroup(level, entry.getKey(), entry.getValue());

        // 2) 消费连边变化队列：根据“是否变为双向连通/断开双向连通”触发 union 或拆分。
        while (true) {
            MananetNetworkManager.EdgeChange change = manager.pollEdgeChange();
            if (change == null) break;
            onNodeConnectivityChanged(level, BlockPos.of(change.posLong()), change.dir(), change.oldValue(), change.newValue());
        }

        // 3) 消费 dirty integrate 队列：为尚未分配 network_id 的节点分配网络并汇总贡献。
        while (true) {
            Long posLong = manager.pollDirty();
            if (posLong == null) break;
            integrateIfNeeded(level, BlockPos.of(posLong));
        }
    }

    public static void onNodeAdded(ServerLevel level, BlockPos pos) {
        markDirty(level, pos);
    }

    /**
     * 处理同一旧网络（oldRoot）上的一组节点移除事件。
     *
     * <p>核心分支：</p>
     * <ul>
     *     <li>移除数 >= 网络规模：网络被清空。</li>
     *     <li>所有移除点都是“叶子”（connectedNeighbors <= 1）：只需扣除贡献，不需要拆分。</li>
     *     <li>可能拆分：在移除点周围找种子，按连通性收集多个分量并重新分配 network_id。</li>
     * </ul>
     */
    private static void processRemovalGroup(ServerLevel level, UUID oldRoot, java.util.ArrayList<MananetNetworkManager.NodeRemoval> removals) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        MananetNetworkManager.NetworkState oldNetwork = manager.getOrCreate(oldRoot);
        // 拆分前先把旧网络的当前 mana 拷贝出来（拆分后需要按 cache 占比重新分配）。
        Mana oldMana = ManaMath.copy(oldNetwork.mana);

        // 移除数量覆盖整个网络：直接把旧网络清空即可（无需再做连通分量计算）。
        if (removals.size() >= oldNetwork.size) {
            manager.setNetwork(oldRoot, new Mana(), new Mana(), new Mana(), 0);
            return;
        }

        // 快路径：若所有被移除节点都不可能充当“割点”（连接邻居数<=1），则不会引起网络拆分。
        boolean needsSplit = false;
        for (MananetNetworkManager.NodeRemoval removal : removals) {
            if (removal.connectedNeighbors() > 1) { needsSplit = true; break; }
        }
        if (!needsSplit) {
            for (MananetNetworkManager.NodeRemoval removal : removals) {
                // 只做贡献扣除：不会改变网络连通结构。
                manager.applyContribution(oldRoot, removal.cache().negative(), removal.load().negative(), -1);
            }
            return;
        }

        // 需要拆分：先把所有被移除位置加入 excluded，后续 BFS 不会经过这些点。
        LongOpenHashSet excluded = new LongOpenHashSet();
        LongArrayList seeds = new LongArrayList();
        LongOpenHashSet seedSet = new LongOpenHashSet();

        for (MananetNetworkManager.NodeRemoval removal : removals) {
            excluded.add(removal.posLong());
        }

        // 从每个被移除点的邻接节点里找“种子”：种子位于旧网络内且与被移除点双向连通。
        for (MananetNetworkManager.NodeRemoval removal : removals) {
            BlockPos removedPos = BlockPos.of(removal.posLong());
            for (Direction dir : Direction.values()) {
                int bit = 1 << dir.ordinal();
                if ((removal.connectivityMask() & bit) == 0) continue;
                BlockPos np = removedPos.relative(dir);
                MananetNodeState ns = manager.getBlockNode(np);
                if (ns == null) continue;
                if (ns.networkId == null) continue;
                if (!manager.resolveNetworkId(ns.networkId).equals(oldRoot)) continue;
                int obit = 1 << dir.getOpposite().ordinal();
                if ((ns.connectivityMask & obit) == 0) continue;
                long key = np.asLong();
                if (excluded.contains(key)) continue;
                if (seedSet.add(key)) seeds.add(key);
            }
        }

        // 从每个种子出发，按连通性收集连通分量（组件），并用 visited 做去重。
        LongOpenHashSet visited = new LongOpenHashSet();
        java.util.ArrayList<Component> components = new java.util.ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            long seed = seeds.getLong(i);
            if (excluded.contains(seed)) continue;
            if (visited.contains(seed)) continue;
            // 在旧网络内，按双方连通掩码做 BFS，收集一个连通分量。
            Component component = collectComponentByState(level, oldRoot, seed, excluded, visited, 0L, null);
            if (component.size > 0) components.add(component);
        }

        // 所有节点都被移除/隔离：旧网络清空。
        if (components.isEmpty()) {
            manager.setNetwork(oldRoot, new Mana(), new Mana(), new Mana(), 0);
            return;
        }

        // 仍为单一分量：只需把 network_id 统一规范化为 oldRoot，并更新汇总 cache/load/size。
        if (components.size() == 1) {
            Component c = components.get(0);
            LongArrayList positions = c.positions;
            for (int i = 0; i < positions.size(); i++) {
                long posLong = positions.getLong(i);
                MananetNodeState ns = manager.getBlockNode(posLong);
                if (ns != null) ns.networkId = oldRoot;
            }
            // 回写 chunk 附件中的 network_id，保证持久化与运行时一致。
            MananetNetworkPersistence.updateNetworkIdBulk(level, positions, oldRoot);
            manager.setNetwork(oldRoot, oldMana, c.cache, c.load, c.size);
            return;
        }

        // 多分量：保留第一个分量沿用 oldRoot，其余分量合并为新网络 idB。
        Component a = components.get(0);
        Mana cacheB = new Mana();
        Mana loadB = new Mana();
        int sizeB = 0;
        LongArrayList positionsB = new LongArrayList();
        for (int i = 1; i < components.size(); i++) {
            Component c = components.get(i);
            cacheB.add(c.cache);
            loadB.add(c.load);
            sizeB += c.size;
            positionsB.addAll(c.positions);
        }

        UUID idB = UUID.randomUUID();

        // 先更新运行时节点状态的 network_id，再批量回写 chunk 附件（避免逐点写入）。
        for (int i = 0; i < a.positions.size(); i++) {
            long posLong = a.positions.getLong(i);
            MananetNodeState ns = manager.getBlockNode(posLong);
            if (ns != null) ns.networkId = oldRoot;
        }
        for (int i = 0; i < positionsB.size(); i++) {
            long posLong = positionsB.getLong(i);
            MananetNodeState ns = manager.getBlockNode(posLong);
            if (ns != null) ns.networkId = idB;
        }
        MananetNetworkPersistence.updateNetworkIdBulk(level, a.positions, oldRoot);
        MananetNetworkPersistence.updateNetworkIdBulk(level, positionsB, idB);

        // 重新计算“旧网络当前 mana 在拆分后如何分配”：先按总 cache 夹紧，再按两侧 cache 占比切分。
        Mana totalCache = ManaMath.copy(a.cache);
        totalCache.add(cacheB);
        Mana clamped = ManaMath.clampToCache(ManaMath.clampNonNegative(oldMana), totalCache);
        // 按两侧缓存占比把旧网络的当前 mana 分给 A/B（逐分量按 cache 比例切分）。
        ManaSplit split = splitManaByCache(clamped, a.cache, cacheB);

        manager.setNetwork(oldRoot, split.manaA, a.cache, a.load, a.size);
        manager.setNetwork(idB, split.manaB, cacheB, loadB, sizeB);
    }

    /**
     * 对某位置执行 integrate（如有必要）。
     *
     * <p>当该位置是节点方块且 {@code networkId == null} 时：</p>
     * <ul>
     *     <li>扫描所有双向连通的邻居节点</li>
     *     <li>若邻居已有网络：选择规模最大的网络作为“挂靠目标”，并 union 其它邻居网络</li>
     *     <li>若没有邻居网络：创建新网络 UUID</li>
     *     <li>写入本节点的 networkId，并把 cache/load/size 贡献汇总到网络</li>
     * </ul>
     */
    public static void integrateIfNeeded(ServerLevel level, BlockPos pos) {
        // integrate 只处理“节点方块”（非方块实体）的接网逻辑。
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        BlockState selfState = level.getBlockState(pos);
        // 方块实体节点：由实体自身实现 MananetNode，这里不参与。
        if (selfState.hasBlockEntity()) return;
        // 非节点方块：直接返回。
        if (!(selfState.getBlock() instanceof AbstractNodeBlock selfBlock)) return;

        // 获取/创建该位置的节点状态（首次创建时由节点方块填充默认 cache/load）。
        MananetNodeState state = manager.getOrCreateBlockNode(pos, () -> {
            MananetNodeState node_state = new MananetNodeState();
            selfBlock.init_node_state(level, pos, selfState, node_state);
            return node_state;
        });
        // 已经有 network_id：说明已经完成接网，无需重复 integrate。
        if (state.networkId != null) return;

        // 收集所有“可双向连通”的邻居网络根 id，并选出规模最大的网络作为优先挂靠目标。
        UUID[] neighborRoots = new UUID[6];
        int neighborRootCount = 0;
        UUID chosen = null;
        int bestSize = -1;

        for (Direction dir : Direction.values()) {
            int bit = 1 << dir.ordinal();
            // 本端该方向未开边：跳过。
            if ((state.connectivityMask & bit) == 0) continue;

            BlockPos np = pos.relative(dir);
            BlockState ns = level.getBlockState(np);
            // 邻居是方块实体：该邻居不走“节点方块状态”路径，跳过。
            if (ns.hasBlockEntity()) continue;
            // 邻居不是节点方块：跳过。
            if (!(ns.getBlock() instanceof AbstractNodeBlock nb)) continue;

            // 获取/创建邻居的节点状态（使得后续能读取其 connectivity/network_id）。
            MananetNodeState neighborState = manager.getOrCreateBlockNode(np, () -> {
                MananetNodeState node_state = new MananetNodeState();
                nb.init_node_state(level, np, ns, node_state);
                return node_state;
            });
            int obit = 1 << dir.getOpposite().ordinal();
            // 邻居反向未开边：不算双向连通。
            if ((neighborState.connectivityMask & obit) == 0) continue;

            // 邻居尚未接网：跳过（等邻居自身 integrate 后再由 dirty/边变化触发合并）。
            if (neighborState.networkId == null) continue;

            // 统一为邻居网络根 id，并去重收集。
            UUID root = manager.resolveNetworkId(neighborState.networkId);
            boolean exists = false;
            for (int i = 0; i < neighborRootCount; i++) {
                if (neighborRoots[i].equals(root)) { exists = true; break; }
            }
            if (exists) continue;
            neighborRoots[neighborRootCount++] = root;

            // 选择规模最大的网络作为“挂靠目标”，减少后续 union 的移动成本。
            int size = manager.getSize(root);
            if (size > bestSize) {
                chosen = root;
                bestSize = size;
            }
        }

        // 周围没有可用网络：创建一个新网络 id，并初始化空网络状态。
        if (chosen == null) {
            chosen = UUID.randomUUID();
            manager.setNetwork(chosen, new Mana(), new Mana(), new Mana(), 0);
        }

        // 把本节点连到 chosen，并把其它邻居网络逐个 union 进来，得到最终根 id。
        for (int i = 0; i < neighborRootCount; i++) chosen = manager.union(chosen, neighborRoots[i]);
        chosen = manager.resolveNetworkId(chosen);

        // 写入本节点归属，并把本节点贡献汇总到网络；最后回写 chunk 附件持久化数据。
        state.networkId = chosen;
        manager.applyContribution(chosen, state.cache, state.load, 1);
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    public static void onNodeGraphUpdated(ServerLevel level, BlockPos pos) {
        markDirty(level, pos);
    }

    public static void onNodeContributionChanged(ServerLevel level, BlockPos pos) {
        markDirty(level, pos);
    }

    public static void onNodeConnectivityChanged(ServerLevel level, BlockPos pos, Direction dir, boolean oldValue, boolean newValue) {
        BlockPos np = pos.relative(dir);
        // 统一把“方块实体节点/节点方块”抽象成 MananetNode 访问器，便于读写 connectivity/network_id。
        MananetNode a = getNodeOrBlockNode(level, pos);
        MananetNode b = getNodeOrBlockNode(level, np);
        if (a == null || b == null) return;

        // 双向连通的判定：本端开边 && 对端反向开边。
        boolean wasConnected = oldValue && b.getConnectivity(dir.getOpposite());
        boolean isConnected = newValue && b.getConnectivity(dir.getOpposite());

        if (!wasConnected && isConnected) {
            // 新连通：确保两端都已 integrate，然后 union 两侧网络。
            integrateIfNeeded(level, pos);
            integrateIfNeeded(level, np);
            // 对节点方块，network_id 存在于 manager.blockNodes；这里直接从 manager 取状态以获取 network_id。
            MananetNodeState sa = MananetNetworkManager.get(level).getBlockNode(pos);
            MananetNodeState sb = MananetNetworkManager.get(level).getBlockNode(np);
            if (sa != null && sb != null && sa.networkId != null && sb.networkId != null) {
                // union-find 合并：把两侧网络汇总为同一根 id（mana/cache/load/size 同步合并）。
                MananetNetworkManager.get(level).union(sa.networkId, sb.networkId);
            }
            return;
        }

        // 断开：如果该边把同一网络切成两部分，则进行拆分。
        if (wasConnected && !isConnected) splitByRemovedEdge(level, pos, np, dir);
    }

    /**
     * 因移除一条边导致的网络拆分。
     *
     * <p>从 aPos 出发按连通性 BFS，若能到达 bPos 则说明仍连通；否则把未访问部分切成新网络。</p>
     */
    private static void splitByRemovedEdge(ServerLevel level, BlockPos aPos, BlockPos bPos, Direction dir) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        // 拿到两端的节点状态（仅节点方块路径）；任一端不存在则无需拆分。
        MananetNodeState aState = getOrCreateNodeState(level, aPos);
        MananetNodeState bState = getOrCreateNodeState(level, bPos);
        if (aState == null || bState == null) return;
        if (aState.networkId == null || bState.networkId == null) return;

        // 两端不在同一网络：断边不会影响网络结构。
        UUID oldId = manager.resolveNetworkId(aState.networkId);
        if (!oldId.equals(manager.resolveNetworkId(bState.networkId))) return;

        // 记录拆分前的当前 mana：拆分后需要重新分配。
        MananetNetworkManager.NetworkState oldNetwork = manager.getOrCreate(oldId);
        Mana oldMana = ManaMath.copy(oldNetwork.mana);

        LongOpenHashSet excluded = new LongOpenHashSet();
        LongOpenHashSet visited = new LongOpenHashSet();
        long aPosLong = aPos.asLong();
        long bPosLong = bPos.asLong();
        // 从 aPos 出发，禁止通过 (aPos -> dir) 这条边，收集 a 所在连通分量。
        Component compA = collectComponentByState(level, oldId, aPosLong, excluded, visited, aPosLong, dir);
        // 若仍能到达 bPos，则说明网络未被断开（依旧连通），无需拆分。
        if (visited.contains(bPosLong)) return;

        // 未访问到的部分即为 B 分量，分配新的 network_id。
        UUID idB = UUID.randomUUID();
        for (int i = 0; i < compA.positions.size(); i++) {
            long posLong = compA.positions.getLong(i);
            MananetNodeState ns = manager.getBlockNode(posLong);
            if (ns != null) ns.networkId = oldId;
        }

        Mana cacheB = new Mana();
        Mana loadB = new Mana();
        int sizeB = 0;
        LongArrayList positionsB = new LongArrayList();
        // 遍历旧网络内所有节点：visited 之外的节点都归入 B 分量，并累计其 cache/load/size。
        for (var entry : manager.iterateBlockNodes()) {
            long posLong = entry.getLongKey();
            MananetNodeState ns = entry.getValue();
            if (ns.networkId == null) continue;
            if (!manager.resolveNetworkId(ns.networkId).equals(oldId)) continue;
            if (visited.contains(posLong)) continue;
            ns.networkId = idB;
            cacheB.add(ns.cache);
            loadB.add(ns.load);
            sizeB++;
            positionsB.add(posLong);
        }
        // 批量回写 chunk 附件中的 network_id，保证持久化与运行时一致。
        MananetNetworkPersistence.updateNetworkIdBulk(level, compA.positions, oldId);
        MananetNetworkPersistence.updateNetworkIdBulk(level, positionsB, idB);

        // 按两侧 cache 占比重新分配当前 mana，并写回两个网络的汇总状态。
        Mana totalCache = ManaMath.copy(compA.cache);
        totalCache.add(cacheB);
        Mana clamped = ManaMath.clampToCache(ManaMath.clampNonNegative(oldMana), totalCache);
        // 拆分后按两侧缓存占比分配当前 mana。
        ManaSplit split = splitManaByCache(clamped, compA.cache, cacheB);

        manager.setNetwork(oldId, split.manaA, compA.cache, compA.load, compA.size);
        manager.setNetwork(idB, split.manaB, cacheB, loadB, sizeB);
    }

    private record ManaSplit(Mana manaA, Mana manaB) {}

    /**
     * 按两侧 cache 占比拆分当前 mana（逐分量分配）。
     *
     * <p>该方法假设 mana 已被夹紧到 {@code cacheA + cacheB} 以内。</p>
     */
    private static ManaSplit splitManaByCache(Mana mana, Mana cacheA, Mana cacheB) {
        double denomR = cacheA.getRadiation() + cacheB.getRadiation();
        double denomT = cacheA.getTemperature() + cacheB.getTemperature();
        double denomM = cacheA.getMomentum() + cacheB.getMomentum();
        double denomP = cacheA.getPressure() + cacheB.getPressure();

        double rA = mana.getRadiation() == 0.0 ? 0.0 : mana.getRadiation() * (cacheA.getRadiation() / denomR);
        double tA = mana.getTemperature() == 0.0 ? 0.0 : mana.getTemperature() * (cacheA.getTemperature() / denomT);
        double mA = mana.getMomentum() == 0.0 ? 0.0 : mana.getMomentum() * (cacheA.getMomentum() / denomM);
        double pA = mana.getPressure() == 0.0 ? 0.0 : mana.getPressure() * (cacheA.getPressure() / denomP);

        Mana manaA = new Mana(rA, tA, mA, pA);
        Mana manaB = ManaMath.copy(mana);
        manaB.add(manaA.negative());
        return new ManaSplit(manaA, manaB);
    }

    /**
     * 将 start 所在的连通分量重新标记为 targetId，并返回该分量的汇总信息。
     *
     * <p>该方法在当前文件内未直接调用，但它把“收集连通分量 + 写回 network_id”的常用套路集中到一起，便于复用。</p>
     */
    private static RebuildResult assignComponentId(ServerLevel level, BlockPos start, UUID targetId, BlockPos excludedPos, BlockPos edgeA, Direction edgeDir) {
        Set<Long> visited = new HashSet<>();
        RebuildResult component = collectComponent(level, start, visited, excludedPos, edgeA, edgeDir);
        for (BlockPos pos : component.positions) {
            MananetNode node = getNodeOrBlockNode(level, pos);
            if (node != null) node.setNetworkId(targetId);
        }
        return new RebuildResult(start, component.positions, component.oldNetworkIds, component.cache, component.load, component.size);
    }

    /**
     * 从 start 出发按“双方连通”收集连通分量（基于 {@link MananetNode} 访问器）。
     *
     * @param excludedPos 若不为 null，则跳过该位置（用于拆分时排除被移除节点）
     * @param edgeA/edgeDir 若不为 null，则把 edgeA 与其在 edgeDir 方向的那条边视为禁用（用于断边拆分）
     */
    private static RebuildResult collectComponent(ServerLevel level, BlockPos start, Set<Long> visited, BlockPos excludedPos, BlockPos edgeA, Direction edgeDir) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        // BFS 队列初始化：从 start 开始向外扩展。
        queue.add(start);

        Set<BlockPos> positions = new HashSet<>();
        Set<UUID> oldIds = new HashSet<>();
        Mana cache = new Mana();
        Mana load = new Mana();
        int size = 0;

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            // 需要排除的点（例如被移除节点）：直接跳过。
            if (excludedPos != null && pos.equals(excludedPos)) continue;
            long key = pos.asLong();
            // visited 去重：保证每个位置只处理一次。
            if (!visited.add(key)) continue;

            // 该位置必须是可访问的节点（方块实体或节点方块）。
            MananetNode node = getNodeOrBlockNode(level, pos);
            if (node == null) continue;

            // 记录该节点并累计其贡献。
            positions.add(pos);
            size++;
            cache.add(node.getCache());
            load.add(node.getLoad());
            if (node.getNetworkId() != null) oldIds.add(node.getNetworkId());

            for (Direction dir : Direction.values()) {
                BlockPos np = pos.relative(dir);
                if (excludedPos != null && np.equals(excludedPos)) continue;
                if (edgeA != null && edgeDir != null && isForbiddenEdge(pos, dir, edgeA, edgeDir)) continue;

                // 邻居也必须是节点，且双方该方向都允许连通。
                MananetNode neighbor = getNodeOrBlockNode(level, np);
                if (neighbor == null) continue;
                if (!node.getConnectivity(dir)) continue;
                if (!neighbor.getConnectivity(dir.getOpposite())) continue;
                queue.addLast(np);
            }
        }

        return new RebuildResult(start, positions, oldIds, cache, load, size);
    }

    /**
     * 判断“某条边”是否是拆分时需要忽略的那条边。
     */
    private static boolean isForbiddenEdge(BlockPos from, Direction dir, BlockPos edgeA, Direction edgeDir) {
        BlockPos edgeB = edgeA.relative(edgeDir);
        if (from.equals(edgeA) && dir == edgeDir) return true;
        return from.equals(edgeB) && dir == edgeDir.getOpposite();
    }

    /**
     * 获取某位置的节点访问器：
     * <ul>
     *     <li>若是实现 {@link MananetNode} 的方块实体：直接返回该实体。</li>
     *     <li>若是 {@link AbstractNodeBlock}：创建一个按位置封装的 {@link MananetBlockNode}。</li>
     *     <li>其它：返回 null。</li>
     * </ul>
     */
    private static MananetNode getNodeOrBlockNode(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MananetNode node) return node;
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) return null;
        if (state.getBlock() instanceof AbstractNodeBlock nodeBlock) return new MananetBlockNode(level, pos, nodeBlock);
        return null;
    }

    /**
     * 获取或创建某位置的节点方块状态（仅适用于 {@link AbstractNodeBlock}）。
     */
    private static MananetNodeState getOrCreateNodeState(ServerLevel level, BlockPos pos) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) return null;
        if (!(state.getBlock() instanceof AbstractNodeBlock block)) return null;
        return manager.getOrCreateBlockNode(pos, () -> {
            MananetNodeState node_state = new MananetNodeState();
            block.init_node_state(level, pos, state, node_state);
            return node_state;
        });
    }

    /**
     * 基于“运行时节点状态”（manager.blockNodes）收集连通分量。
     *
     * <p>该版本避免频繁创建 {@link MananetBlockNode}，更适合在拆分等重计算路径中使用。</p>
     */
    private static Component collectComponentByState(
            ServerLevel level,
            UUID oldRoot,
            long startPosLong,
            LongOpenHashSet excluded,
            LongOpenHashSet visited,
            long edgeA,
            Direction edgeDir
    ) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        // BFS 队列初始化：从起点位置（posLong）开始向外扩展。
        queue.enqueue(startPosLong);

        LongArrayList positions = new LongArrayList();
        Mana cache = new Mana();
        Mana load = new Mana();
        int size = 0;

        // 若指定了“禁止通过的边”，则预先计算边的另一端（edgeB）。
        long edgeB = edgeDir == null ? 0L : BlockPos.of(edgeA).relative(edgeDir).asLong();
        while (!queue.isEmpty()) {
            long posLong = queue.dequeueLong();
            if (excluded.contains(posLong)) continue;
            // visited 去重：保证每个位置只处理一次。
            if (!visited.add(posLong)) continue;

            // 必须存在运行时节点状态，且已接网，并属于 oldRoot（按根 id 对比）。
            MananetNodeState state = manager.getBlockNode(posLong);
            if (state == null) continue;
            if (state.networkId == null) continue;
            if (!manager.resolveNetworkId(state.networkId).equals(oldRoot)) continue;
            // 顺手把非根 network_id 规范化为 oldRoot（让后续对比/回写更一致）。
            if (!oldRoot.equals(state.networkId)) state.networkId = oldRoot;

            // 记录该节点并累计其贡献。
            positions.add(posLong);
            size++;
            cache.add(state.cache);
            load.add(state.load);

            for (Direction dir : Direction.values()) {
                int bit = 1 << dir.ordinal();
                // 本端该方向未开边：跳过。
                if ((state.connectivityMask & bit) == 0) continue;
                if (edgeDir != null) {
                    // 禁止通过指定断边（edgeA <-> edgeB）。
                    if (posLong == edgeA && dir == edgeDir) continue;
                    if (posLong == edgeB && dir == edgeDir.getOpposite()) continue;
                }

                // 计算邻居位置（npLong），并跳过 excluded 集合中的点。
                int x = BlockPos.getX(posLong) + dir.getStepX();
                int y = BlockPos.getY(posLong) + dir.getStepY();
                int z = BlockPos.getZ(posLong) + dir.getStepZ();
                long npLong = BlockPos.asLong(x, y, z);
                if (excluded.contains(npLong)) continue;

                // 邻居必须有状态、反向开边、已接网、且同属 oldRoot。
                MananetNodeState ns = manager.getBlockNode(npLong);
                if (ns == null) continue;
                int obit = 1 << dir.getOpposite().ordinal();
                if ((ns.connectivityMask & obit) == 0) continue;
                if (ns.networkId == null) continue;
                if (!manager.resolveNetworkId(ns.networkId).equals(oldRoot)) continue;
                // 满足双向连通：入队继续 BFS。
                queue.enqueue(npLong);
            }
        }

        return new Component(positions, cache, load, size);
    }

    private record Component(LongArrayList positions, Mana cache, Mana load, int size) {}

    private record RebuildResult(BlockPos startPos, Set<BlockPos> positions, Set<UUID> oldNetworkIds, Mana cache, Mana load, int size) {}
}
