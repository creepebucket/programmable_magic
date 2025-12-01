package org.creepebucket.programmable_magic.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModItems;
import org.creepebucket.programmable_magic.registries.ModRecipeSerializers;

/**
 * 无序合成：占位符 + 任意其他物品 → 绑定该物品的占位符。
 */
public class BindWandItemPlaceholderRecipe extends CustomRecipe {

    public BindWandItemPlaceholderRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // 要求：恰好 1 个占位符 + 1 个非空、非占位符物品，其余为空
        int placeholder = 0;
        int others = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack st = input.getItem(i);
            if (st.isEmpty()) continue;
            if (st.getItem() instanceof WandItemPlaceholder) { placeholder++; continue; }
            others++;
        }
        return placeholder == 1 && others == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack placeholder = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack st = input.getItem(i);
            if (st.isEmpty()) continue;
            if (st.getItem() instanceof WandItemPlaceholder) placeholder = st;
            else target = st;
        }
        if (placeholder.isEmpty() || target.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(ModItems.WAND_ITEM_PLACEHOLDER.get());
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(target.getItem());
        if (key != null) out.set(ModDataComponents.WAND_PLACEHOLDER_ITEM_ID.get(), key.toString());
        return out;
    }

    // 1.21.8 的 CustomRecipe 不再要求 canCraftInDimensions，保留按需实现即可

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.BIND_WAND_ITEM_PLACEHOLDER.get();
    }

    public static final class Serializer implements RecipeSerializer<BindWandItemPlaceholderRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        private static final BindWandItemPlaceholderRecipe DUMMY = new BindWandItemPlaceholderRecipe(CraftingBookCategory.MISC);

        public static final MapCodec<BindWandItemPlaceholderRecipe> CODEC = MapCodec.unit(DUMMY);
        public static final StreamCodec<RegistryFriendlyByteBuf, BindWandItemPlaceholderRecipe> STREAM_CODEC = StreamCodec.unit(DUMMY);

        @Override
        public MapCodec<BindWandItemPlaceholderRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BindWandItemPlaceholderRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
