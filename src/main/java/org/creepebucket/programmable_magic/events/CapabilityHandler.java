package org.creepebucket.programmable_magic.events;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.registries.MananetNodeBlocks;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;

import java.util.List;

public class CapabilityHandler {

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		for (var block : List.of(
				MananetNodeBlocks.WIND_TURBINE.get(),
				MananetNodeBlocks.SOLAR_PANEL.get(),
				MananetNodeBlocks.HEAT_EXCHANGER.get(),
				MananetNodeBlocks.STEAM_TURBINE.get(),
				MananetNodeBlocks.PRESSURE_RELIEF_VALVE.get(),
				MananetNodeBlocks.WATER_PUMP.get(),
				MananetNodeBlocks.LIQUID_HEATER.get()
		)) {
			event.registerBlock(Capabilities.Item.BLOCK, (level, pos, state, be, side) ->
					((MachineBlockEntity) be).getItemHandler(new RelativeBlockPos(0, 0, 0)), block);
			event.registerBlock(Capabilities.Fluid.BLOCK, (level, pos, state, be, side) ->
					((MachineBlockEntity) be).getFluidHandler(new RelativeBlockPos(0, 0, 0)), block);
		}
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.IO_DUMMY.get(), (be, side) -> be.getItemHandler());
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.IO_DUMMY.get(), (be, side) -> be.getFluidHandler());
	}
}
