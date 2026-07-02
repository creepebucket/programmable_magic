package org.creepebucket.programmable_magic.mananet.machines.consumer.steam_boiler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.mananet.machines.RotatableBasicMachine;
import org.jspecify.annotations.Nullable;

public class SteamBoiler extends RotatableBasicMachine {

	public SteamBoiler(Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		addFluidInput(-1, 0, 0, 1, 8000);
		addFluidOutput(1, 0, 0, 1, 8000);

		return new SteamBoilerBlockEntity(pos, state);
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
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		return InteractionResult.PASS;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
	}
}
