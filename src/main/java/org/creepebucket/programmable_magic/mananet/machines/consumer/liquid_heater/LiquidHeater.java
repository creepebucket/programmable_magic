package org.creepebucket.programmable_magic.mananet.machines.consumer.liquid_heater;

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
import org.creepebucket.programmable_magic.gui.machines.consumer.liquid_heater.LiquidHeaterMenu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;
import org.jspecify.annotations.Nullable;

public class LiquidHeater extends BasicMachine {

	public LiquidHeater(Properties properties) {
		super(properties);

		addFluidInput(new RelativeBlockPos(-1, 0, 0));
		addFluidOutput(new RelativeBlockPos(1, 0, 0));
		addItemInput(new RelativeBlockPos(1, 1, 0));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LiquidHeaterBlockEntity(pos, state);
	}

	public VoxelShape hitbox() {
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(-1, 0, -1, 2, 0.1875, 2), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(1.426776875, 0.135723125, 1.5625, 1.676776875, 0.948223125, 1.8125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.25, 0.25, 1.6875, 0.75, 0.75, 2), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.676776875, 0.135723125, -0.8125, -0.42677687500000006, 0.948223125, -0.5625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(1.426776875, 0.135723125, -0.8125, 1.676776875, 0.948223125, -0.5625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.25, 0.25, -1, 0.75, 0.75, -0.6875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.25, 1.25, -1, 0.75, 1.75, -0.6875), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.5, 0.1875, -0.9375, 1.5, 2, 1.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.676776875, 0.135723125, 1.5625, -0.42677687500000006, 0.948223125, 1.8125), BooleanOp.OR);

		return shape;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
				(containerId, inventory, p) -> new LiquidHeaterMenu(containerId, inventory, pos),
				Component.literal("")
		);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.LIQUID_HEATER_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> LiquidHeaterBlockEntity.tick(lvl, pos, st, (LiquidHeaterBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
