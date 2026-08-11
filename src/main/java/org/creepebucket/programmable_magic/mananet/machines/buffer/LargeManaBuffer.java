package org.creepebucket.programmable_magic.mananet.machines.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.gui.machines.buffer.ManaBufferMenu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.jspecify.annotations.Nullable;

public class LargeManaBuffer extends BasicMachine {

	public LargeManaBuffer(Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LargeManaBufferBlockEntity(pos, state);
	}

	@Override
	public VoxelShape hitbox() {
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(-1, 0, -1, 2, 0.25, 2), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.5625, 0.25, -0.5625, 1.5625, 1.0625, 1.5625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.6875, 2.75, -0.6875, 1.6875, 2.875, 1.6875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.625, 1.0625, -0.625, 1.625, 3.6875, 1.625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 3.6875, 0, 1, 4, 1), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.6875, 1.875, -0.6875, 1.6875, 2, 1.6875), BooleanOp.OR);

		return shape;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider((containerId, inventory, p) -> {
			var be = (ManaBufferBlockEntity) level.getBlockEntity(pos);
			return new ManaBufferMenu(containerId, inventory, pos, be.baseStorage, be.baseExpansion, be.baseExpansionPower, be.maxChargePower, be.chargeSlotCount);
		}, Component.literal(""));
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.LARGE_MANA_BUFFER_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> ManaBufferBlockEntity.tick(lvl, pos, st, (ManaBufferBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
