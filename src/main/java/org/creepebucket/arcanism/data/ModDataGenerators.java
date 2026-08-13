package org.creepebucket.arcanism.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater.LiquidHeaterRecipies;
import org.creepebucket.arcanism.mananet.machines.generator.heat_exchanger.HeatExchangerRecipies;
import org.creepebucket.arcanism.mananet.machines.generator.pressure_relief_valve.PressureReliefValveRecipies;
import org.creepebucket.arcanism.mananet.machines.generator.steam_turbine.SteamTurbineRecipies;
import org.creepebucket.arcanism.registries.ModBlockTagProvider;
import org.creepebucket.arcanism.registries.ModItemTagProvider;

public class ModDataGenerators {
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModBlockTagProvider::new);
        event.createProvider(output -> new ModItemTagProvider(output, event.getLookupProvider()));
        event.createProvider(SpellItemModelProvider::new);
        event.createProvider(WandPluginItemModelProvider::new);
        event.createProvider(GeneralItemModelProvider::new);
    }

	public static void gatherServerData(GatherDataEvent.Client event) {
        event.createProvider(LiquidHeaterRecipies.Runner::new);
        event.createProvider(HeatExchangerRecipies.Runner::new);
        event.createProvider(SteamTurbineRecipies.Runner::new);
        event.createProvider(PressureReliefValveRecipies.Runner::new);
    }
}
