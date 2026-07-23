package org.creepebucket.programmable_magic.mananet.machines.generator.heat_exchanger;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
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
import org.creepebucket.programmable_magic.gui.machines.generator.heat_exchanger.HeatExchangerMenu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;
import org.jspecify.annotations.Nullable;

public class HeatExchanger extends BasicMachine {

	public HeatExchanger(Properties properties) {
		super(properties);
		addFluidInput(new RelativeBlockPos(-1, 0, 0));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HeatExchangerBlockEntity(pos, state);
	}

	public VoxelShape hitbox() {
		VoxelShape shape = Shapes.empty();

		shape = Shapes.join(shape, Shapes.box(-1, 0, -1, 2, 0.1875, 2), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.6875, 0.1875, 1.59375, -0.375, 0.75, 1.78125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.6875, 0.1875, -0.78125, -0.4375, 0.75, -0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(1.375, 0.1875, -0.78125, 1.6875, 0.75, -0.59375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(1.3125, 0.1875, 1.59375, 1.6875, 0.75, 1.78125), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.5, 0.125, -0.9375, 1.5, 2, 1.9375), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(-0.6875, 0.1875, -0.9375, 1.75, 0.375, 1.9375), BooleanOp.OR);

		return shape;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> {
				buf.writeBlockPos(pos);
			});
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
				(containerId, inventory, p) -> new HeatExchangerMenu(containerId, inventory, pos),
				Component.literal("")
		);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.HEAT_EXCHANGER_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> HeatExchangerBlockEntity.tick(lvl, pos, st, (HeatExchangerBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
