package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.machines.consumer.liquid_heater.LiquidHeaterRecipies;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModRecipeBookCategories {
	public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES =
			DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, MODID);

	public static final Supplier<RecipeBookCategory> LIQUID_HEATER =
			RECIPE_BOOK_CATEGORIES.register("liquid_heater", RecipeBookCategory::new);

	public static void register(IEventBus bus) {
		RECIPE_BOOK_CATEGORIES.register(bus);
	}
}
