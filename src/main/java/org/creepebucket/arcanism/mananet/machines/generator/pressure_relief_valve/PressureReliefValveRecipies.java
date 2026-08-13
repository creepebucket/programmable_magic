package org.creepebucket.arcanism.mananet.machines.generator.pressure_relief_valve;

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

public record PressureReliefValveRecipies(CommonInfo commonInfo, String inputFluid, double inputAmount, double heatPerLiter) implements Recipe<PressureReliefValveRecipies.Input> {

	public static void buildRecipes(RecipeOutput output) {
		addRecipe(output, "steam_to_pressure", "arcanism:steam", 30, (double) 4_000_000 / 30);
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
		return ModRecipeSerializers.PRESSURE_RELIEF_VALVE.get();
	}

	@Override
	public RecipeType<? extends Recipe<Input>> getType() {
		return ModRecipeTypes.PRESSURE_RELIEF_VALVE.get();
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipeBookCategories.PRESSURE_RELIEF_VALVE.get();
	}

	@Override
	public boolean showNotification() {
		return commonInfo.showNotification();
	}

	@Override
	public String group() {
		return "";
	}

	public static final MapCodec<PressureReliefValveRecipies> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
			Codec.STRING.fieldOf("input_fluid").forGetter(r -> r.inputFluid),
			Codec.DOUBLE.fieldOf("input_amount").forGetter(r -> r.inputAmount),
			Codec.DOUBLE.fieldOf("heat_per_liter").forGetter(r -> r.heatPerLiter)
	).apply(inst, PressureReliefValveRecipies::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PressureReliefValveRecipies> STREAM_CODEC = StreamCodec.composite(
			CommonInfo.STREAM_CODEC, r -> r.commonInfo,
			ByteBufCodecs.STRING_UTF8, r -> r.inputFluid,
			ByteBufCodecs.DOUBLE, r -> r.inputAmount,
			ByteBufCodecs.DOUBLE, r -> r.heatPerLiter,
			PressureReliefValveRecipies::new
	);

	public static final RecipeSerializer<PressureReliefValveRecipies> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public static void addRecipe(RecipeOutput output, String id, String inputFluid, double inputAmount, double heatPerLiter) {
		var recipe = new PressureReliefValveRecipies(
				new CommonInfo(false),
				inputFluid,
				inputAmount,
				heatPerLiter
		);
		output.accept(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MODID, "pressure_relief_valve/" + id)), recipe, null);
	}

	public static class Provider extends RecipeProvider {
		public Provider(HolderLookup.Provider registries, RecipeOutput output) {
			super(registries, output);
		}

		@Override
		protected void buildRecipes() {
			PressureReliefValveRecipies.buildRecipes(output);
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
			return "Pressure Relief Valve Recipes";
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
