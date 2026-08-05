package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.machines.consumer.liquid_heater.LiquidHeaterRecipies;
import org.creepebucket.programmable_magic.mananet.machines.generator.heat_exchanger.HeatExchangerRecipies;
import org.creepebucket.programmable_magic.mananet.machines.generator.pressure_relief_valve.PressureReliefValveRecipies;
import org.creepebucket.programmable_magic.mananet.machines.generator.steam_turbine.SteamTurbineRecipies;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModRecipeTypes {
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
			DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

	public static final Supplier<RecipeType<LiquidHeaterRecipies>> LIQUID_HEATER =
			RECIPE_TYPES.register("liquid_heater", RecipeType::simple);

	public static final Supplier<RecipeType<HeatExchangerRecipies>> HEAT_EXCHANGER =
			RECIPE_TYPES.register("heat_exchanger", RecipeType::simple);

	public static final Supplier<RecipeType<SteamTurbineRecipies>> STEAM_TURBINE =
			RECIPE_TYPES.register("steam_turbine", RecipeType::simple);

	public static final Supplier<RecipeType<PressureReliefValveRecipies>> PRESSURE_RELIEF_VALVE =
			RECIPE_TYPES.register("pressure_relief_valve", RecipeType::simple);

	public static void register(IEventBus bus) {
		RECIPE_TYPES.register(bus);
	}
}
