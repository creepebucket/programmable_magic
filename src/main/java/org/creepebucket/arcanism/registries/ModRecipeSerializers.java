package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater.LiquidHeaterRecipies;
import org.creepebucket.arcanism.mananet.machines.generator.heat_exchanger.HeatExchangerRecipies;
import org.creepebucket.arcanism.mananet.machines.generator.pressure_relief_valve.PressureReliefValveRecipies;
import org.creepebucket.arcanism.mananet.machines.generator.steam_turbine.SteamTurbineRecipies;
import org.creepebucket.arcanism.recipes.BindWandItemPlaceholderRecipe;

import java.util.function.Supplier;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final Supplier<RecipeSerializer<BindWandItemPlaceholderRecipe>> BIND_WAND_ITEM_PLACEHOLDER =
            RECIPE_SERIALIZERS.register("bind_wand_item_placeholder",
                    () -> BindWandItemPlaceholderRecipe.SERIALIZER);

    public static final Supplier<RecipeSerializer<LiquidHeaterRecipies>> LIQUID_HEATER =
            RECIPE_SERIALIZERS.register("liquid_heater",
                    () -> LiquidHeaterRecipies.SERIALIZER);

    public static final Supplier<RecipeSerializer<HeatExchangerRecipies>> HEAT_EXCHANGER =
            RECIPE_SERIALIZERS.register("heat_exchanger",
                    () -> HeatExchangerRecipies.SERIALIZER);

    public static final Supplier<RecipeSerializer<SteamTurbineRecipies>> STEAM_TURBINE =
            RECIPE_SERIALIZERS.register("steam_turbine",
                    () -> SteamTurbineRecipies.SERIALIZER);

    public static final Supplier<RecipeSerializer<PressureReliefValveRecipies>> PRESSURE_RELIEF_VALVE =
            RECIPE_SERIALIZERS.register("pressure_relief_valve",
                    () -> PressureReliefValveRecipies.SERIALIZER);

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
