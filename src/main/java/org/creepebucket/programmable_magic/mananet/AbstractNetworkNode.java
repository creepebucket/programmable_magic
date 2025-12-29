package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

/**
 * 抽象网络节点：描述一类“可连接机器”的行为与元数据。
 *
 * - tick：方块实体每刻回调（服务端/客户端根据实现自行判断）。
 * - getDataComponentTypes：需要的 DataComponentType 定义（默认空）。
 * - getRegistryData：注册所需的基础信息（注册名/本地化键/模型材质等）。
 */
public abstract class AbstractNetworkNode extends AbstractNetNodeBlock {
    private final Supplier<BlockEntityType<NodeBoundBlockEntity>> beType;

    protected AbstractNetworkNode(Properties props, Supplier<BlockEntityType<NodeBoundBlockEntity>> beType) {
        super(props);
        this.beType = beType;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeBoundBlockEntity(beType.get(), pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> {
            if (lvl.isClientSide) return;
            if (be instanceof NodeBoundBlockEntity nbe) NodeBoundBlockEntity.tick(lvl, p, st, nbe);
        };
    }

    /**
     * 每刻回调，由注册器绑定到对应方块实体的 ticker。
     */
    public void tick(NodeBoundBlockEntity be) {}
}
