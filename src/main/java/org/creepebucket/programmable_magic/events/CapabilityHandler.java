package org.creepebucket.programmable_magic.events;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class CapabilityHandler {

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ITEM_INPUT.get(), (be, side) -> be.wrapper);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ITEM_OUTPUT.get(), (be, side) -> be.wrapper);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FLUID_INPUT.get(), (be, side) -> be.wrapper);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FLUID_OUTPUT.get(), (be, side) -> be.wrapper);
	}
}
