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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

public class DummyBlockEntities {
	public static class ItemInput extends BlockEntity {
		public static final int SIZE = 16;
		public static BlockEntityType<ItemInput> TYPE;

		public Container container;
		public ResourceHandler<ItemResource> wrapper;

		public ItemInput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
			container = new SimpleContainer(SIZE) {
				@Override
				public void setChanged() {
					super.setChanged();
					ItemInput.this.setChanged();
				}
			};
			wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(container), true, false);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			container = new SimpleContainer(SIZE) {
				@Override
				public void setChanged() {
					super.setChanged();
					ItemInput.this.setChanged();
				}
			};
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
		public static final int SIZE = 16;
		public static BlockEntityType<ItemOutput> TYPE;

		public Container container;
		public ResourceHandler<ItemResource> wrapper;

		public ItemOutput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
			container = new SimpleContainer(SIZE) {
				@Override
				public void setChanged() {
					super.setChanged();
					ItemOutput.this.setChanged();
				}
			};
			wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(container), false, true);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			container = new SimpleContainer(SIZE) {
				@Override
				public void setChanged() {
					super.setChanged();
					ItemOutput.this.setChanged();
				}
			};
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
		public static final int SIZE = 1;
		public static BlockEntityType<FluidInput> TYPE;

		public FluidStacksResourceHandler fluidHandler;
		public ResourceHandler<FluidResource> wrapper;
		public Container fluidIoContainer;

		public FluidInput(BlockPos worldPosition, BlockState blockState, int capacity) {
			super(TYPE, worldPosition, blockState);
			fluidHandler = new FluidStacksResourceHandler(SIZE, capacity) {
				@Override
				protected void onContentsChanged(int index, FluidStack previousContents) {
					super.onContentsChanged(index, previousContents);
					FluidInput.this.setChanged();
				}
			};
			wrapper = new FlowControlHandler<>(fluidHandler, true, false);
			fluidIoContainer = new SimpleContainer(2) {
				@Override
				public void setChanged() {
					super.setChanged();
					FluidInput.this.setChanged();
				}
			};
		}

		public FluidInput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			int capacity = input.getIntOr("tank_capacity", 0);
			fluidHandler = new FluidStacksResourceHandler(SIZE, capacity) {
				@Override
				protected void onContentsChanged(int index, FluidStack previousContents) {
					super.onContentsChanged(index, previousContents);
					FluidInput.this.setChanged();
				}
			};
			fluidHandler.deserialize(input);
			wrapper = new FlowControlHandler<>(fluidHandler, true, false);
			fluidIoContainer = new SimpleContainer(2) {
				@Override
				public void setChanged() {
					super.setChanged();
					FluidInput.this.setChanged();
				}
			};
			ContainerHelper.loadAllItems(input, ((SimpleContainer) fluidIoContainer).getItems());
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
			if (fluidIoContainer != null)
				ContainerHelper.saveAllItems(output, ((SimpleContainer) fluidIoContainer).getItems(), true);
		}
	}

	public static class FluidOutput extends BlockEntity {
		public static final int SIZE = 1;
		public static BlockEntityType<FluidOutput> TYPE;

		public FluidStacksResourceHandler fluidHandler;
		public ResourceHandler<FluidResource> wrapper;
		public Container fluidIoContainer;

		public FluidOutput(BlockPos worldPosition, BlockState blockState, int capacity) {
			super(TYPE, worldPosition, blockState);
			fluidHandler = new FluidStacksResourceHandler(SIZE, capacity) {
				@Override
				protected void onContentsChanged(int index, FluidStack previousContents) {
					super.onContentsChanged(index, previousContents);
					FluidOutput.this.setChanged();
				}
			};
			wrapper = new FlowControlHandler<>(fluidHandler, false, true);
			fluidIoContainer = new SimpleContainer(2) {
				@Override
				public void setChanged() {
					super.setChanged();
					FluidOutput.this.setChanged();
				}
			};
		}

		public FluidOutput(BlockPos worldPosition, BlockState blockState) {
			super(TYPE, worldPosition, blockState);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			int capacity = input.getIntOr("tank_capacity", 0);
			fluidHandler = new FluidStacksResourceHandler(SIZE, capacity) {
				@Override
				protected void onContentsChanged(int index, FluidStack previousContents) {
					super.onContentsChanged(index, previousContents);
					FluidOutput.this.setChanged();
				}
			};
			fluidHandler.deserialize(input);
			wrapper = new FlowControlHandler<>(fluidHandler, false, true);
			fluidIoContainer = new SimpleContainer(2) {
				@Override
				public void setChanged() {
					super.setChanged();
					FluidOutput.this.setChanged();
				}
			};
			ContainerHelper.loadAllItems(input, ((SimpleContainer) fluidIoContainer).getItems());
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
			if (fluidIoContainer != null)
				ContainerHelper.saveAllItems(output, ((SimpleContainer) fluidIoContainer).getItems(), true);
		}
	}
}
