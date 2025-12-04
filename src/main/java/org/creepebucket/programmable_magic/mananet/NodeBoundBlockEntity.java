package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mananet.AbstractNetNodeBlockEntity;

/**
 * 绑定到某个 AbstractNetworkNode 的方块实体实现。
 */
public class NodeBoundBlockEntity extends AbstractNetNodeBlockEntity {
    private final AbstractNetworkNode node;

    public NodeBoundBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, AbstractNetworkNode node) {
        super(type, pos, state);
        this.node = node;
    }

    public AbstractNetworkNode getNode() { return node; }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeBoundBlockEntity be) {
        var net = be.getNet();
        if (net == null) return;
        be.node.tick(be);
    }
}
