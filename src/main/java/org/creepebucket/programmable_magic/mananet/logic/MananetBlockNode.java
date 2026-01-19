package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;
import org.creepebucket.programmable_magic.mananet.api.MananetNode;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

import java.util.UUID;

/**
 * 节点方块（{@link AbstractNodeBlock}）的“按位置”访问器实现。
 *
 * <p>由于 {@link net.minecraft.world.level.block.Block} 是单例对象，节点方块的实例状态必须外置：
 * 该访问器把“世界 + 坐标 + 方块类型”绑定成一个临时对象，并把读写转发到：</p>
 * <ul>
 *     <li>{@link MananetNetworkManager}：运行时缓存与网络汇总</li>
 *     <li>{@link MananetNetworkPersistence}：chunk 附件持久化</li>
 *     <li>{@link MananetNetworkLogic}：需要时触发 integrate/结构更新</li>
 * </ul>
 */
final class MananetBlockNode implements MananetNode {

    private final ServerLevel level;
    private final BlockPos pos;
    private final AbstractNodeBlock block;
    private final MananetNetworkManager manager;
    private final MananetNodeState state;

    MananetBlockNode(ServerLevel level, BlockPos pos, AbstractNodeBlock block) {
        this.level = level;
        this.pos = pos;
        this.block = block;
        // 运行时管理器：用于访问/更新该位置节点状态，以及该节点所属网络的汇总状态。
        this.manager = MananetNetworkManager.get(level);
        // 节点方块状态初始化依赖方块状态（例如不同状态决定 cache/load/connectivity 默认值）。
        BlockState state = level.getBlockState(pos);
        this.state = manager.getOrCreateBlockNode(pos, () -> {
            MananetNodeState node_state = new MananetNodeState();
            // 首次创建该位置的节点状态：交由节点方块填充默认 cache/load 等。
            block.init_node_state(level, pos, state, node_state);
            return node_state;
        });
    }

    @Override
    public Mana getCache() {
        return state.cache;
    }

    @Override
    public void setCache(Mana mana) {
        // 记录旧 cache：用于计算对网络汇总 cache 的增量。
        Mana prev = state.cache;
        // 更新该节点状态中的 cache。
        state.cache = mana;
        // 获取当前网络根 id（未接网则为 null）。
        UUID id = getNetworkId();
        // 已接入网络则直接更新汇总贡献；否则标记 dirty 等待 integrate。
        if (id != null) manager.applyContribution(id, ManaMath.delta(mana, prev), new Mana(), 0);
        else MananetNetworkLogic.markDirty(level, pos);
        // 回写 chunk 附件，持久化该位置节点数据。
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public Mana getLoad() {
        return state.load;
    }

    @Override
    public void setLoad(Mana mana) {
        // 记录旧 load：用于计算对网络汇总 load 的增量。
        Mana prev = state.load;
        // 更新该节点状态中的 load（每秒净负载）。
        state.load = mana;
        // 获取当前网络根 id（未接网则为 null）。
        UUID id = getNetworkId();
        // 已接入网络则直接更新汇总贡献；否则标记 dirty 等待 integrate。
        if (id != null) manager.applyContribution(id, new Mana(), ManaMath.delta(mana, prev), 0);
        else MananetNetworkLogic.markDirty(level, pos);
        // 回写 chunk 附件，持久化该位置节点数据。
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public void addMana(Mana mana) {
        // 直接对所属网络的当前 availableMana 做增量修改（与 load 的持续变化是两套机制）。
        manager.addMana(getNetworkId(), mana);
    }

    @Override
    public boolean getConnectivity(Direction direction) {
        int bit = 1 << direction.ordinal();
        return (state.connectivityMask & bit) != 0;
    }

    @Override
    public void setConnectivity(Direction direction, boolean connectivity) {
        // 先读旧值：用于生成“边变化事件”（old/new）。
        boolean old = getConnectivity(direction);
        if (old == connectivity) return;
        int bit = 1 << direction.ordinal();
        // 只修改本端掩码位，是否真正连通由双方掩码共同决定。
        if (connectivity) state.connectivityMask |= bit; else state.connectivityMask &= ~bit;
        // 连边变化不在这里直接做合并/拆分：排队交给逻辑层在 tick 中统一处理。
        MananetNetworkLogic.enqueueConnectivityChange(level, pos, direction, old, connectivity);
        // 回写 chunk 附件，持久化 connectivity_mask。
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public Mana getMana() {
        return manager.getMana(getNetworkId());
    }

    @Override
    public boolean canProduce() {
        return manager.canProduce(getNetworkId());
    }

    @Override
    public UUID getNetworkId() {
        return manager.resolveNetworkId(state.networkId);
    }

    @Override
    public void setNetworkId(UUID networkId) {
        // 写入节点归属网络 id（是否为根 id 由持久化层与逻辑层在后续规范化）。
        state.networkId = networkId;
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public Level getNodeLevel() {
        return level;
    }

    @Override
    public BlockPos getNodePos() {
        return pos;
    }

    @Override
    public String getNodeRegistryId() {
        return block.getNodeRegistryId();
    }
}
