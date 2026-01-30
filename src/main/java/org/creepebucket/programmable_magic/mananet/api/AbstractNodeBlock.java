package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.logic.MananetNetworkLogic;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * “节点方块”的基类。
 *
 * <p>该类继承 {@link Block} 并实现 {@link MananetNode}，但<strong>它本身不是节点实例</strong>：
 * {@link Block} 是单例对象，不能在其中保存“每个方块位置不同”的状态。</p>
 *
 * <p>节点方块的真实状态由运行时管理器按位置存放（见 {@code MananetNetworkManager} 的 {@code blockNodes}），
 * 访问时会由逻辑层在需要的位置创建一个轻量访问器（见 {@code MananetBlockNode}）。</p>
 *
 * <p>因此，本类对 {@link MananetNode} 的绝大多数方法都直接抛异常，避免误把 “Block 单例” 当作 “节点实例”。</p>
 */
public abstract class AbstractNodeBlock extends Block implements MananetNode {

    protected AbstractNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public final void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // 节点方块放置后，交由逻辑层在后续 tick 中完成 integrate（分配 network_id / 合并邻接网络）。
        if (level instanceof ServerLevel serverLevel && !state.hasBlockEntity())
            MananetNetworkLogic.markDirty(serverLevel, pos);
    }

    @Override
    protected final void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        // 节点方块被移除后，把“移除事件 + 该节点的贡献”排队交给逻辑层处理（可能触发网络拆分）。
        if (!state.hasBlockEntity()) MananetNetworkLogic.enqueueBlockRemoval(level, pos, this);
    }

    @Override
    protected final void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    /**
     * 初始化节点方块的按位置状态。
     *
     * <p>该方法只会在该位置第一次创建 {@link MananetNodeState} 时调用一次；后续读写将直接复用缓存状态。</p>
     *
     * <p>典型用途：</p>
     * <ul>
     *     <li>设置 {@link MananetNodeState#cache}（容量）</li>
     *     <li>设置 {@link MananetNodeState#load}（每秒净负载）</li>
     *     <li>设置默认的 {@link MananetNodeState#connectivityMask}</li>
     * </ul>
     */
    public void init_node_state(ServerLevel level, BlockPos pos, BlockState state, MananetNodeState node_state) {
    }

    @Override
    public final Mana getCache() {
        throw new UnsupportedOperationException("node_block_has_no_instance_cache");
    }

    @Override
    public final void setCache(Mana mana) {
        throw new UnsupportedOperationException("node_block_has_no_instance_cache");
    }

    @Override
    public final Mana getLoad() {
        throw new UnsupportedOperationException("node_block_has_no_instance_load");
    }

    @Override
    public final void setLoad(Mana mana) {
        throw new UnsupportedOperationException("node_block_has_no_instance_load");
    }

    @Override
    public final void addMana(Mana mana) {
        throw new UnsupportedOperationException("node_block_has_no_instance_mana");
    }

    @Override
    public final boolean getConnectivity(Direction direction) {
        throw new UnsupportedOperationException("node_block_has_no_instance_connectivity");
    }

    @Override
    public final void setConnectivity(Direction direction, boolean connectivity) {
        throw new UnsupportedOperationException("node_block_has_no_instance_connectivity");
    }

    @Override
    public final Mana getMana() {
        throw new UnsupportedOperationException("node_block_has_no_instance_mana");
    }

    @Override
    public final boolean canProduce() {
        throw new UnsupportedOperationException("node_block_has_no_instance_can_produce");
    }

    @Override
    public final UUID getNetworkId() {
        throw new UnsupportedOperationException("node_block_has_no_instance_network_id");
    }

    @Override
    public final void setNetworkId(UUID networkId) {
        throw new UnsupportedOperationException("node_block_has_no_instance_network_id");
    }

    @Override
    public final Level getNodeLevel() {
        throw new UnsupportedOperationException("node_block_has_no_instance_level");
    }

    @Override
    public final BlockPos getNodePos() {
        throw new UnsupportedOperationException("node_block_has_no_instance_pos");
    }

    @Override
    public final String getNodeRegistryId() {
        return getNodeRegistryIdInternal();
    }

    /**
     * 返回该节点方块的注册 id（不含命名空间）。
     */
    protected abstract String getNodeRegistryIdInternal();
}
