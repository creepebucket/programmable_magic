package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.registries.MananetNodeBlocks;
import org.jspecify.annotations.Nullable;

public abstract class RotatableBasicMachine extends BasicMachine {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	public RotatableBasicMachine(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction dir = context.getHorizontalDirection().getOpposite();
		if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) dir = dir.getOpposite();

		var level = context.getLevel();
		var pos = context.getClickedPos();
		for (var offset : DUMMY_OFFSETS) {
			var rotated = rotateOffset(offset, dir);
			if (!level.getBlockState(pos.offset(rotated)).canBeReplaced()) return null;
		}
		return defaultBlockState().setValue(FACING, dir);
	}


	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof Level actual_level && !actual_level.isClientSide()) {
			Direction dir = state.getValue(FACING);
			for (var offset : DUMMY_OFFSETS) {
				var rotated = rotateOffset(offset, dir);
				var dummy_pos = pos.offset(rotated);
				var dummy_state = actual_level.getBlockState(dummy_pos);
                if (!(dummy_state.getBlock() instanceof DummyBlock)) continue;
				if (!DummyBlock.get_main_pos(dummy_pos, dummy_state).equals(pos)) continue;
				actual_level.setBlock(
						dummy_pos,
						actual_level.getFluidState(dummy_pos).createLegacyBlock(),
						Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS
				);
			}
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return rotate(HITBOX, state.getValue(FACING));
	}
}
