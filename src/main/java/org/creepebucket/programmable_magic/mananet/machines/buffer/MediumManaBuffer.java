package org.creepebucket.programmable_magic.mananet.machines.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.jspecify.annotations.Nullable;

public class MediumManaBuffer extends BasicMachine {

	public MediumManaBuffer(Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MediumManaBufferBlockEntity(pos, state);
	}

	@Override
	public VoxelShape hitbox() {
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.1875, 1), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 0.5, 0, 1, 3, 1), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);

		return shape;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.MEDIUM_MANA_BUFFER_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> MediumManaBufferBlockEntity.tick(lvl, pos, st, (MediumManaBufferBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
