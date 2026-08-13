package org.creepebucket.arcanism.mananet.machines.generator.steam_turbine;

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
import org.creepebucket.arcanism.gui.machines.generator.steam_turbine.SteamTurbineMenu;
import org.creepebucket.arcanism.mananet.NetNodeBlockEntity;
import org.creepebucket.arcanism.mananet.machines.BasicMachine;
import org.creepebucket.arcanism.registries.ModBlockEntities;
import org.creepebucket.arcanism.utils.RelativeBlockPos;
import org.jspecify.annotations.Nullable;

public class SteamTurbine extends BasicMachine {

	public SteamTurbine(Properties properties) {
		super(properties);
		addFluidInput(new RelativeBlockPos(1, 0, 0));
		addFluidOutput(new RelativeBlockPos(0, 2, 0));
		addManaLink(new RelativeBlockPos(1, 1, 0));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SteamTurbineBlockEntity(pos, state);
	}

	public VoxelShape hitbox() {
		VoxelShape shape = Shapes.empty();

		shape = Shapes.join(shape, Shapes.box(-1, 0, -1, 2, 0.1875, 2), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.75, 0.1875, -0.75, 1.75, 0.375, 1.75), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.125, 1, -0.3125, 1.125, 4.5, 1.3125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.3125, 1, -0.125, 1.3125, 4.5, 1.125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 0.1875, -1, 1, 1, -0.8125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.0625, 0.1875, -0.8125, 1.0625, 1, -0.5625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.5625, 0.375, -0.5625, 1.5625, 1, 1.5625), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 4.4375, 0, 1, 4.8125, 1), BooleanOp.OR);

		return shape;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
				(containerId, inventory, p) -> new SteamTurbineMenu(containerId, inventory, pos),
				Component.literal("")
		);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.STEAM_TURBINE_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> SteamTurbineBlockEntity.tick(lvl, pos, st, (SteamTurbineBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
