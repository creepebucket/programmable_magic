package org.creepebucket.programmable_magic.mananet.machines;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public interface IMachineIo {
	interface FluidOutput {
		ResourceHandler<FluidResource> getFluidOutput();
	}

	interface FluidInput {
		ResourceHandler<FluidResource> getFluidInput();
	}

	interface ItemOutput {
		ResourceHandler<ItemResource> getItemOutput();
	}

	interface ItemInput {
		ResourceHandler<ItemResource> getItemInput();
	}
}
