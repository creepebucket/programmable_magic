package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.recipes.BindWandItemPlaceholderRecipe;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final Supplier<RecipeSerializer<BindWandItemPlaceholderRecipe>> BIND_WAND_ITEM_PLACEHOLDER =
            RECIPE_SERIALIZERS.register("bind_wand_item_placeholder",
                    () -> BindWandItemPlaceholderRecipe.Serializer.INSTANCE);

    public static void register(IEventBus bus) { RECIPE_SERIALIZERS.register(bus); }
}
