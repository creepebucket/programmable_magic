package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;
import org.creepebucket.programmable_magic.gui.machines.io_dummy.IoDummyMenu;
import org.jspecify.annotations.Nullable;

public class IoDummies {

	public static class ItemInputBlock extends DummyBlock implements EntityBlock {
		public int size;

		public ItemInputBlock(Properties properties, int size) {
			super(properties);
			this.size = size;
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.ItemInput(pos, state, size);
		}

		@Override
		protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> buf.writeBlockPos(pos));
			}
			return InteractionResult.SUCCESS;
		}

		@Override
		protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
			return new SimpleMenuProvider(
					(containerId, inventory, p) -> new IoDummyMenu(containerId, inventory, pos),
					Component.literal("")
			);
		}
		@Override
		public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
			return ItemStack.EMPTY;
		}
	}

	public static class ItemOutputBlock extends DummyBlock implements EntityBlock {
		public int size;

		public ItemOutputBlock(Properties properties, int size) {
			super(properties);
			this.size = size;
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.ItemOutput(pos, state, size);
		}

		@Override
		protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> buf.writeBlockPos(pos));
			}
			return InteractionResult.SUCCESS;
		}

		@Override
		protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
			return new SimpleMenuProvider(
					(containerId, inventory, p) -> new IoDummyMenu(containerId, inventory, pos),
					Component.literal("")
			);
		}
		@Override
		public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
			return ItemStack.EMPTY;
		}
	}

	public static class FluidInputBlock extends DummyBlock implements EntityBlock {
		public int size;
		public int capacity;

		public FluidInputBlock(Properties properties, int size, int capacity) {
			super(properties);
			this.size = size;
			this.capacity = capacity;
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.FluidInput(pos, state, size, capacity);
		}

		@Override
		protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> buf.writeBlockPos(pos));
			}
			return InteractionResult.SUCCESS;
		}

		@Override
		protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
			return new SimpleMenuProvider(
					(containerId, inventory, p) -> new IoDummyMenu(containerId, inventory, pos),
					Component.literal("")
			);
		}
		@Override
		public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
			return ItemStack.EMPTY;
		}
	}

	public static class FluidOutputBlock extends DummyBlock implements EntityBlock {
		public int size;
		public int capacity;

		public FluidOutputBlock(Properties properties, int size, int capacity) {
			super(properties);
			this.size = size;
			this.capacity = capacity;
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.FluidOutput(pos, state, size, capacity);
		}

		@Override
		protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> buf.writeBlockPos(pos));
			}
			return InteractionResult.SUCCESS;
		}

		@Override
		protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
			return new SimpleMenuProvider(
					(containerId, inventory, p) -> new IoDummyMenu(containerId, inventory, pos),
					Component.literal("")
			);
		}
		@Override
		public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
			return ItemStack.EMPTY;
		}
	}
}
