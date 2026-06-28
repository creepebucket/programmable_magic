package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;

public class MachineBlockEntity extends NetNodeBlockEntity implements IMachineIo.FluidOutput, IMachineIo.FluidInput, IMachineIo.ItemOutput, IMachineIo.ItemInput {

	public FluidHandler fluidOutputHandler;
	public FluidHandler fluidInputHandler;

	public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	public FluidHandler createFluidHandler(long capacity) {
		var handler = new FluidHandler(capacity);
		handler.onChanged = this::setChanged;
		return handler;
	}

	@Override
	public ResourceHandler<FluidResource> getFluidOutput() {
		return fluidOutputHandler;
	}

	@Override
	public ResourceHandler<FluidResource> getFluidInput() {
		return fluidInputHandler;
	}

	@Override
	public ResourceHandler<ItemResource> getItemOutput() {
		return null;
	}

	@Override
	public ResourceHandler<ItemResource> getItemInput() {
		return null;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		if (fluidOutputHandler != null) {
			fluidOutputHandler.capacity = input.getLongOr("fluid_out_cap", fluidOutputHandler.capacity);
			fluidOutputHandler.tank = input.read("fluid_out", FluidStack.CODEC).orElse(FluidStack.EMPTY);
		}
		if (fluidInputHandler != null) {
			fluidInputHandler.capacity = input.getLongOr("fluid_in_cap", fluidInputHandler.capacity);
			fluidInputHandler.tank = input.read("fluid_in", FluidStack.CODEC).orElse(FluidStack.EMPTY);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (fluidOutputHandler != null) {
			output.putLong("fluid_out_cap", fluidOutputHandler.capacity);
			output.store("fluid_out", FluidStack.CODEC, fluidOutputHandler.tank);
		}
		if (fluidInputHandler != null) {
			output.putLong("fluid_in_cap", fluidInputHandler.capacity);
			output.store("fluid_in", FluidStack.CODEC, fluidInputHandler.tank);
		}
	}
}
