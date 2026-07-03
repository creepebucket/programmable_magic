package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;
import org.creepebucket.programmable_magic.gui.machines.io_dummy.IoDummyMenu;
import org.jspecify.annotations.Nullable;

public class IoDummies {

	public static abstract class AbstractIoDummyBlock extends DummyBlock implements EntityBlock {

		public AbstractIoDummyBlock(Properties properties) {
			super(properties);
		}

		@Override
		public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				var container = switch (level.getBlockEntity(pos)) {
					case DummyBlockEntities.ItemInput ii -> ii.container;
					case DummyBlockEntities.ItemOutput io -> io.container;
					case DummyBlockEntities.FluidInput fi -> fi.fluidIoContainer;
					case DummyBlockEntities.FluidOutput fo -> fo.fluidIoContainer;
					default -> null;
				};
				if (container != null) {
					Containers.dropContents(level, pos, container);
				}
				serverPlayer.gameMode.destroyBlock(get_main_pos(pos, state));
			}
			return state;
		}

		@Override
		public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
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

	public static class ItemInputBlock extends AbstractIoDummyBlock {

		public ItemInputBlock(Properties properties) {
			super(properties);
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.ItemInput(pos, state);
		}
	}

	public static class ItemOutputBlock extends AbstractIoDummyBlock {

		public ItemOutputBlock(Properties properties) {
			super(properties);
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.ItemOutput(pos, state);
		}
	}

	public static class FluidInputBlock extends AbstractIoDummyBlock {
		public int capacity;

		public FluidInputBlock(Properties properties, int capacity) {
			super(properties);
			this.capacity = capacity;
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.FluidInput(pos, state, capacity);
		}
	}

	public static class FluidOutputBlock extends AbstractIoDummyBlock {
		public int capacity;

		public FluidOutputBlock(Properties properties, int capacity) {
			super(properties);
			this.capacity = capacity;
		}

		@Override
		public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new DummyBlockEntities.FluidOutput(pos, state, capacity);
		}
	}
}
