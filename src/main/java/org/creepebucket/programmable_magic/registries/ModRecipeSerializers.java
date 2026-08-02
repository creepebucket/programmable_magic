package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.machines.consumer.liquid_heater.LiquidHeaterRecipies;
import org.creepebucket.programmable_magic.mananet.machines.generator.heat_exchanger.HeatExchangerRecipies;
import org.creepebucket.programmable_magic.recipes.BindWandItemPlaceholderRecipe;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

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

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
