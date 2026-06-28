package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (level.isClientSide()) return;

		Direction dir = state.getValue(FACING);
		var dummy_block = MananetNodeBlocks.DUMMY_BLOCK.get();
		for (var offset : DUMMY_OFFSETS) {
			var rotated = rotateOffset(offset, dir);
			var dummy_pos = pos.offset(rotated);
			level.setBlock(
					dummy_pos,
					dummy_block.defaultBlockState()
							.setValue(DummyBlock.X_OFFSET, -rotated.getX())
							.setValue(DummyBlock.Y_OFFSET, -rotated.getY())
							.setValue(DummyBlock.Z_OFFSET, -rotated.getZ()),
					Block.UPDATE_ALL
			);
		}
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof Level actual_level && !actual_level.isClientSide()) {
			Direction dir = state.getValue(FACING);
			for (var offset : DUMMY_OFFSETS) {
				var rotated = rotateOffset(offset, dir);
				var dummy_pos = pos.offset(rotated);
				var dummy_state = actual_level.getBlockState(dummy_pos);
				if (!dummy_state.is(MananetNodeBlocks.DUMMY_BLOCK)) continue;
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

	public static VoxelShape rotate(VoxelShape shape, Direction direction) {
		if (direction == Direction.NORTH) return shape;
		VoxelShape result = Shapes.empty();
		for (AABB aabb : shape.toAabbs()) {
			result = Shapes.or(result, switch (direction) {
				case SOUTH -> Shapes.box(1 - aabb.maxX, aabb.minY, 1 - aabb.maxZ, 1 - aabb.minX, aabb.maxY, 1 - aabb.minZ);
				case WEST -> Shapes.box(aabb.minZ, aabb.minY, 1 - aabb.maxX, aabb.maxZ, aabb.maxY, 1 - aabb.minX);
				case EAST -> Shapes.box(1 - aabb.maxZ, aabb.minY, aabb.minX, 1 - aabb.minZ, aabb.maxY, aabb.maxX);
				default -> Shapes.box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
			});
		}
		return result;
	}

	public static BlockPos rotateOffset(BlockPos offset, Direction direction) {
		return switch (direction) {
			case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
			case WEST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
			case EAST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
			default -> offset;
		};
	}
}
