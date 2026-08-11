package org.creepebucket.programmable_magic.mananet.machines.generator.pressure_relief_valve;

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
import org.creepebucket.programmable_magic.gui.machines.generator.pressure_relief_valve.PressureReliefValveMenu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;
import org.jspecify.annotations.Nullable;

public class PressureReliefValve extends BasicMachine {

	public PressureReliefValve(Properties properties) {
		super(properties);
		addFluidInput(new RelativeBlockPos(0, 0, 0));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PressureReliefValveBlockEntity(pos, state);
	}

	public VoxelShape hitbox() {
		VoxelShape shape = Shapes.empty();

		shape = Shapes.join(shape, Shapes.box(0.28125, 0.28125, 0.8125, 0.71875, 0.71875, 0.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.25, 0.25, 0.9375, 0.75, 0.75, 1.0625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.1875, 0.25, 0.1875, 0.8125, 0.875, 0.8125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.9375, 0.25, 0.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.125, 0.875, 0.125, 0.875, 1.0625, 0.875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.15625, 1.75, 0.15625, 0.84375, 2.125, 0.84375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.1875, 1.0625, 0.1875, 0.8125, 1.75, 0.8125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 2.5625, 0, 1, 2.9375, 1), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.09375, 2.375, 0.09375, 0.90625, 2.625, 0.90625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 4.75, 0, 1, 5, 1), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.0625, 2.875, 0.0625, 0.9375, 4.5625, 0.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.03125, 4.5625, 0.03125, 0.96875, 4.75, 0.96875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.125, 1.21875, 0.125, 0.875, 1.40625, 0.875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.125, 1.5625, 0.125, 0.875, 1.75, 0.875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.03125, 2.0625, 0.03125, 0.96875, 2.375, 0.96875), BooleanOp.OR);

		return shape;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
				(containerId, inventory, p) -> new PressureReliefValveMenu(containerId, inventory, pos),
				Component.literal("")
		);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.PRESSURE_RELIEF_VALVE_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> PressureReliefValveBlockEntity.tick(lvl, pos, st, (PressureReliefValveBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
