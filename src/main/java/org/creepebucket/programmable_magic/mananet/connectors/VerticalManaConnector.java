package org.creepebucket.programmable_magic.mananet.connectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;

import java.util.function.Supplier;

public class VerticalManaConnector extends AbstractManaConnector {
	public VerticalManaConnector(Properties properties, Supplier<BlockEntityType<NetNodeBlockEntity>> blockEntityType) {
		super(properties, blockEntityType);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(0.375, 0, 0.375, 0.625, 0.9375, 0.625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.4375, 0.9375, 0.4375, 0.5625, 1, 0.5625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.125, 0.25, 0.125, 0.875, 0.4, 0.875), BooleanOp.OR);

		return shape;
	}

	@Override
	protected boolean isValidConnectionFace(Direction face) {
		return face.getAxis().isHorizontal();
	}
}
