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
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;
import org.jspecify.annotations.Nullable;

public class SmallManaBuffer extends BasicMachine {

	public SmallManaBuffer(Properties properties) {
		super(properties);
		addManaLink(new RelativeBlockPos(0, 1, 0));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SmallManaBufferBlockEntity(pos, state);
	}

	@Override
	public VoxelShape hitbox() {
		return Shapes.box(0, 0, 0, 1, 1, 1);
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
		if (type == ModBlockEntities.SMALL_MANA_BUFFER_BLOCK_ENTITY.get()) {
			return (lvl, pos, st, blockEntity) -> ManaBufferBlockEntity.tick(lvl, pos, st, (ManaBufferBlockEntity) blockEntity);
		}
		return null;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		NetNodeBlockEntity.rebuildNetworkId(level, pos);
	}
}
