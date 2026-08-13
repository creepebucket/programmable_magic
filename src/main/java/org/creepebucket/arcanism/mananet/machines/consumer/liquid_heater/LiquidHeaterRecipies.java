package org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater;

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

public record LiquidHeaterRecipies(CommonInfo commonInfo, String inputFluid, String outputFluid, double conversionCost, double convertRatio) implements Recipe<LiquidHeaterRecipies.Input> {

	public static void buildRecipes(RecipeOutput output) {
		addRecipe(output, "water_to_steam", "minecraft:water", "arcanism:steam", 4_000_000, 30d);
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
		return ModRecipeSerializers.LIQUID_HEATER.get();
	}

	@Override
	public RecipeType<? extends Recipe<Input>> getType() {
		return ModRecipeTypes.LIQUID_HEATER.get();
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipeBookCategories.LIQUID_HEATER.get();
	}

	@Override
	public boolean showNotification() {
		return commonInfo.showNotification();
	}

	@Override
	public String group() {
		return "";
	}

	public static final MapCodec<LiquidHeaterRecipies> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
			Codec.STRING.fieldOf("input_fluid").forGetter(r -> r.inputFluid),
			Codec.STRING.fieldOf("output_fluid").forGetter(r -> r.outputFluid),
			Codec.DOUBLE.fieldOf("conversion_cost").forGetter(r -> r.conversionCost),
			Codec.DOUBLE.fieldOf("convert_ratio").forGetter(r -> r.convertRatio)
	).apply(inst, LiquidHeaterRecipies::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LiquidHeaterRecipies> STREAM_CODEC = StreamCodec.composite(
			CommonInfo.STREAM_CODEC, r -> r.commonInfo,
			ByteBufCodecs.STRING_UTF8, r -> r.inputFluid,
			ByteBufCodecs.STRING_UTF8, r -> r.outputFluid,
			ByteBufCodecs.DOUBLE, r -> r.conversionCost,
			ByteBufCodecs.DOUBLE, r -> r.convertRatio,
			LiquidHeaterRecipies::new
	);

	public static final RecipeSerializer<LiquidHeaterRecipies> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public static void addRecipe(RecipeOutput output, String id, String inputFluid, String outputFluid, double conversionCost, double convertRatio) {
		var recipe = new LiquidHeaterRecipies(
				new CommonInfo(false),
				inputFluid,
				outputFluid,
				conversionCost,
				convertRatio
		);
		output.accept(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MODID, "liquid_heater/" + id)), recipe, null);
	}

	public static class Provider extends RecipeProvider {
		public Provider(HolderLookup.Provider registries, RecipeOutput output) {
			super(registries, output);
		}

		@Override
		protected void buildRecipes() {
			LiquidHeaterRecipies.buildRecipes(output);
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
			return "Liquid Heater Recipes";
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
