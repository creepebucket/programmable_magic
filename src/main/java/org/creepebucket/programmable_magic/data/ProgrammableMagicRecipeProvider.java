package org.creepebucket.programmable_magic.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import org.creepebucket.programmable_magic.registries.ModItems;

import java.util.concurrent.CompletableFuture;

    /** 合成表 Datagen（NeoForge 1.21.8）。 */
public class ProgrammableMagicRecipeProvider extends RecipeProvider {

    protected ProgrammableMagicRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.DEBRIS_DUST.get(), 4)
                .requires(Items.NETHERITE_SCRAP)
                .unlockedBy("has_netherite_scrap", this.has(Items.NETHERITE_SCRAP))
                .save(this.output, "programmable_magic:debris_dust_from_netherite_scrap");

        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.DEBRIS_CLAY.get())
                .pattern("CCC")
                .pattern("CDC")
                .pattern("CCC")
                .define('C', Items.CLAY_BALL)
                .define('D', ModItems.DEBRIS_DUST.get())
                .unlockedBy("has_debris_dust", this.has(ModItems.DEBRIS_DUST.get()))
                .save(this.output);

        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.RG_ALLOY_WIRE.get(), 3)
                .pattern("X  ")
                .pattern(" X ")
                .pattern("  X")
                .define('X', ModItems.REDSTONE_GOLD_ALLOY.get())
                .unlockedBy("has_redstone_gold_alloy", this.has(ModItems.REDSTONE_GOLD_ALLOY.get()))
                .save(this.output, "programmable_magic:rg_alloy_wire_diagonal");

        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.RG_ALLOY_ROD.get())
                .pattern("X")
                .pattern("X")
                .pattern("X")
                .define('X', ModItems.REDSTONE_GOLD_ALLOY.get())
                .unlockedBy("has_redstone_gold_alloy", this.has(ModItems.REDSTONE_GOLD_ALLOY.get()))
                .save(this.output, "programmable_magic:rg_alloy_rod_vertical");

        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.COVERED_RG_ALLOY_WIRE.get())
                .requires(ModItems.RG_ALLOY_WIRE.get())
                .requires(net.minecraft.world.item.crafting.Ingredient.of(ModItems.DEBRIS_CLAY.get()), 2)
                .unlockedBy("has_rg_alloy_wire", this.has(ModItems.RG_ALLOY_WIRE.get()))
                .unlockedBy("has_debris_clay", this.has(ModItems.DEBRIS_CLAY.get()))
                .save(this.output);

        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.SMALL_CELL_CERTIDGE.get())
                .pattern("RWR")
                .pattern("WCW")
                .pattern("RWR")
                .define('R', ModItems.RG_ALLOY_ROD.get())
                .define('W', ModItems.COVERED_RG_ALLOY_WIRE.get())
                .define('C', ModItems.DEBRIS_CLAY.get())
                .unlockedBy("has_rg_alloy_rod", this.has(ModItems.RG_ALLOY_ROD.get()))
                .unlockedBy("has_covered_wire", this.has(ModItems.COVERED_RG_ALLOY_WIRE.get()))
                .unlockedBy("has_debris_clay", this.has(ModItems.DEBRIS_CLAY.get()))
                .save(this.output);

        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.RG_ALLOY_WAND.get())
                .pattern("SCA")
                .pattern("SWC")
                .pattern("WSS")
                .define('S', Items.STICK)
                .define('W', ModItems.COVERED_RG_ALLOY_WIRE.get())
                .define('C', ModItems.DEBRIS_CLAY.get())
                .define('A', ModItems.REDSTONE_GOLD_ALLOY.get())
                .unlockedBy("has_rg_alloy_rod", this.has(ModItems.RG_ALLOY_ROD.get()))
                .unlockedBy("has_covered_wire", this.has(ModItems.COVERED_RG_ALLOY_WIRE.get()))
                .unlockedBy("has_debris_clay", this.has(ModItems.DEBRIS_CLAY.get()))
                .save(this.output);

        ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.SMALL_CELL_SHELL.get())
                .pattern("T")
                .pattern("I")
                .pattern("T")
                .define('T', Items.IRON_TRAPDOOR)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_trapdoor", this.has(Items.IRON_TRAPDOOR))
                .save(this.output);

        ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, ModItems.SMALL_MANA_CELL_DEFERRED_ITEM.get())
                .requires(ModItems.SMALL_CELL_SHELL.get())
                .requires(ModItems.SMALL_CELL_CERTIDGE.get())
                .unlockedBy("has_small_cell_shell", this.has(ModItems.SMALL_CELL_SHELL.get()))
                .unlockedBy("has_small_cell_certidge", this.has(ModItems.SMALL_CELL_CERTIDGE.get()))
                .save(this.output);

        // 特殊合成：占位符 + 任意物品 → 绑定该物品的占位符
        SpecialRecipeBuilder.special(org.creepebucket.programmable_magic.recipes.BindWandItemPlaceholderRecipe::new)
                .save(this.output, "programmable_magic:bind_wand_item_placeholder");
    }

    // Runner：注册到 GatherDataEvent 使用
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ProgrammableMagicRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Programmable Magic Recipes";
        }
    }
}
