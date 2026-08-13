package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModRecipeBookCategories {
	public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES =
			DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, MODID);

	public static final Supplier<RecipeBookCategory> LIQUID_HEATER =
			RECIPE_BOOK_CATEGORIES.register("liquid_heater", RecipeBookCategory::new);

	public static final Supplier<RecipeBookCategory> HEAT_EXCHANGER =
			RECIPE_BOOK_CATEGORIES.register("heat_exchanger", RecipeBookCategory::new);

	public static final Supplier<RecipeBookCategory> STEAM_TURBINE =
			RECIPE_BOOK_CATEGORIES.register("steam_turbine", RecipeBookCategory::new);

	public static final Supplier<RecipeBookCategory> PRESSURE_RELIEF_VALVE =
			RECIPE_BOOK_CATEGORIES.register("pressure_relief_valve", RecipeBookCategory::new);

	public static void register(IEventBus bus) {
		RECIPE_BOOK_CATEGORIES.register(bus);
	}
}
