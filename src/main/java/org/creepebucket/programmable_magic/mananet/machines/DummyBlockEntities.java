package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

public class DummyBlockEntities {
	public static class ItemInput extends BlockEntity {
		public static BlockEntityType<ItemInput> TYPE;

		public Container container;
		public ResourceHandler<ItemResource> wrapper;

		public ItemInput(BlockPos worldPosition, BlockState blockState, int size) {
			super(TYPE, worldPosition, blockState);
			container = new SimpleContainer(size);
			wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(container), true, false);
		}

		public ItemInput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			int size = input.getIntOr("container_size", 0);
			container = new SimpleContainer(size);
			ContainerHelper.loadAllItems(input, ((SimpleContainer) container).getItems());
			wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(container), true, false);
		}

		@Override
		protected void saveAdditional(ValueOutput output) {
			super.saveAdditional(output);
			if (container == null) {
				return;
			}
			output.putInt("container_size", container.getContainerSize());
			ContainerHelper.saveAllItems(output, ((SimpleContainer) container).getItems(), true);
		}
	}

	public static class ItemOutput extends BlockEntity {
		public static BlockEntityType<ItemOutput> TYPE;

		public Container container;
		public ResourceHandler<ItemResource> wrapper;

		public ItemOutput(BlockPos worldPosition, BlockState blockState, int size) {
			super(TYPE, worldPosition, blockState);
			container = new SimpleContainer(size);
			wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(container), false, true);
		}

		public ItemOutput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			int size = input.getIntOr("container_size", 0);
			container = new SimpleContainer(size);
			ContainerHelper.loadAllItems(input, ((SimpleContainer) container).getItems());
			wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(container), false, true);
		}

		@Override
		protected void saveAdditional(ValueOutput output) {
			super.saveAdditional(output);
			if (container == null) {
				return;
			}
			output.putInt("container_size", container.getContainerSize());
			ContainerHelper.saveAllItems(output, ((SimpleContainer) container).getItems(), true);
		}
	}

	public static class FluidInput extends BlockEntity {
		public static BlockEntityType<FluidInput> TYPE;

		public FluidStacksResourceHandler fluidHandler;
		public ResourceHandler<FluidResource> wrapper;

		public FluidInput(BlockPos worldPosition, BlockState blockState, int size, int capacity) {
			super(TYPE, worldPosition, blockState);
			fluidHandler = new FluidStacksResourceHandler(size, capacity);
			wrapper = new FlowControlHandler<>(fluidHandler, true, false);
		}

		public FluidInput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			int size = input.getIntOr("tank_count", 0);
			int capacity = input.getIntOr("tank_capacity", 0);
			fluidHandler = new FluidStacksResourceHandler(size, capacity);
			fluidHandler.deserialize(input);
			wrapper = new FlowControlHandler<>(fluidHandler, true, false);
		}

		@Override
		protected void saveAdditional(ValueOutput output) {
			super.saveAdditional(output);
			if (fluidHandler == null) {
				return;
			}
			output.putInt("tank_count", fluidHandler.size());
			output.putInt("tank_capacity", fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY));
			fluidHandler.serialize(output);
		}
	}

	public static class FluidOutput extends BlockEntity {
		public static BlockEntityType<FluidOutput> TYPE;

		public FluidStacksResourceHandler fluidHandler;
		public ResourceHandler<FluidResource> wrapper;

		public FluidOutput(BlockPos worldPosition, BlockState blockState, int size, int capacity) {
			super(TYPE, worldPosition, blockState);
			fluidHandler = new FluidStacksResourceHandler(size, capacity);
			wrapper = new FlowControlHandler<>(fluidHandler, false, true);
		}

		public FluidOutput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			int size = input.getIntOr("tank_count", 0);
			int capacity = input.getIntOr("tank_capacity", 0);
			fluidHandler = new FluidStacksResourceHandler(size, capacity);
			fluidHandler.deserialize(input);
			wrapper = new FlowControlHandler<>(fluidHandler, false, true);
		}

		@Override
		protected void saveAdditional(ValueOutput output) {
			super.saveAdditional(output);
			if (fluidHandler == null) {
				return;
			}
			output.putInt("tank_count", fluidHandler.size());
			output.putInt("tank_capacity", fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY));
			fluidHandler.serialize(output);
		}
	}
}
