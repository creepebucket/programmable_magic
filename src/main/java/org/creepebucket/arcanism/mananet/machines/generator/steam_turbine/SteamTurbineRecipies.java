package org.creepebucket.arcanism.mananet.machines.generator.steam_turbine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.creepebucket.arcanism.registries.ModRecipeBookCategories;
import org.creepebucket.arcanism.registries.ModRecipeSerializers;
import org.creepebucket.arcanism.registries.ModRecipeTypes;

import java.util.concurrent.CompletableFuture;

import static org.creepebucket.arcanism.Arcanism.MODID;

public record SteamTurbineRecipies(CommonInfo commonInfo, String inputFluid, double inputAmount, String outputFluid, double outputAmount, double heatPerLiter) implements Recipe<SteamTurbineRecipies.Input> {

	public static void buildRecipes(RecipeOutput output) {
		addRecipe(output, "steam_to_water", "arcanism:steam", 30, "minecraft:water", 1, (double) 4_000_000 / 30);
	}

	@Override
	public boolean matches(Input input, Level level) {
		return Identifier.parse(this.inputFluid).equals(input.fluidId);
	}

	@Override
	public ItemStack assemble(Input input) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<? extends Recipe<Input>> getSerializer() {
		return ModRecipeSerializers.STEAM_TURBINE.get();
	}

	@Override
	public RecipeType<? extends Recipe<Input>> getType() {
		return ModRecipeTypes.STEAM_TURBINE.get();
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipeBookCategories.STEAM_TURBINE.get();
	}

	@Override
	public boolean showNotification() {
		return commonInfo.showNotification();
	}

	@Override
	public String group() {
		return "";
	}

	public static final MapCodec<SteamTurbineRecipies> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
			Codec.STRING.fieldOf("input_fluid").forGetter(r -> r.inputFluid),
			Codec.DOUBLE.fieldOf("input_amount").forGetter(r -> r.inputAmount),
			Codec.STRING.fieldOf("output_fluid").forGetter(r -> r.outputFluid),
			Codec.DOUBLE.fieldOf("output_amount").forGetter(r -> r.outputAmount),
			Codec.DOUBLE.fieldOf("heat_per_liter").forGetter(r -> r.heatPerLiter)
	).apply(inst, SteamTurbineRecipies::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SteamTurbineRecipies> STREAM_CODEC = StreamCodec.composite(
			CommonInfo.STREAM_CODEC, r -> r.commonInfo,
			ByteBufCodecs.STRING_UTF8, r -> r.inputFluid,
			ByteBufCodecs.DOUBLE, r -> r.inputAmount,
			ByteBufCodecs.STRING_UTF8, r -> r.outputFluid,
			ByteBufCodecs.DOUBLE, r -> r.outputAmount,
			ByteBufCodecs.DOUBLE, r -> r.heatPerLiter,
			SteamTurbineRecipies::new
	);

	public static final RecipeSerializer<SteamTurbineRecipies> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public static void addRecipe(RecipeOutput output, String id, String inputFluid, double inputAmount, String outputFluid, double outputAmount, double heatPerLiter) {
		var recipe = new SteamTurbineRecipies(
				new CommonInfo(false),
				inputFluid,
				inputAmount,
				outputFluid,
				outputAmount,
				heatPerLiter
		);
		output.accept(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MODID, "steam_turbine/" + id)), recipe, null);
	}

	public static class Provider extends RecipeProvider {
		public Provider(HolderLookup.Provider registries, RecipeOutput output) {
			super(registries, output);
		}

		@Override
		protected void buildRecipes() {
			SteamTurbineRecipies.buildRecipes(output);
		}
	}

	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
			super(output, lookupProvider);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
			return new Provider(registries, output);
		}

		@Override
		public String getName() {
			return "Steam Turbine Recipes";
		}
	}

	public record Input(Identifier fluidId) implements RecipeInput {
		@Override
		public boolean isEmpty() { return false; }

		@Override
		public ItemStack getItem(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public int size() {
			return 0;
		}
	}
}
