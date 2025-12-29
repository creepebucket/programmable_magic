package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 绑定到某个 AbstractNetworkNode 的方块实体实现。
 */
public class NodeBoundBlockEntity extends AbstractNetNodeBlockEntity {
    public NodeBoundBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeBoundBlockEntity be) {
        var net = be.getNet();
        if (net == null) return;
        if (state.getBlock() instanceof AbstractNetworkNode node) node.tick(be);
    }
}
