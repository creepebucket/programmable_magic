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

public class SmallManaBuffer extends BasicMachine {

	public SmallManaBuffer(Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SmallManaBufferBlockEntity(pos, state);
	}

	@Override
	public VoxelShape hitbox() {
		return Shapes.box(0, 0, 0, 1, 1, 1);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.SMALL_MANA_BUFFER_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> SmallManaBufferBlockEntity.tick(lvl, pos, st, (SmallManaBufferBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
