package org.creepebucket.arcanism.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.arcanism.items.api.ModItemExtensions;
import org.creepebucket.arcanism.registries.ModDataComponents;

import java.util.ArrayList;
import java.util.List;

public class PackedSpell extends Item implements ModItemExtensions {
    public PackedSpell(Properties properties) {
        super(properties.component(ModDataComponents.RESOURCE_LOCATION, "item/packed_spell_default.png")
                .component(ModDataComponents.AUTHER, "")
                .component(ModDataComponents.DESCRIPTION, "")
                .component(ModDataComponents.SPELLS, new ArrayList<>()));
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        tooltip.add(Component.translatable("tooltip.arcanism.packed_spell.auther").append(Component.literal(stack.get(ModDataComponents.AUTHER)).withStyle(ChatFormatting.GREEN)));
        tooltip.add(Component.translatable("tooltip.arcanism.packed_spell.description"));
        tooltip.add(Component.literal("    ").append(Component.literal(stack.get(ModDataComponents.DESCRIPTION)).withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.translatable("tooltip.arcanism.packed_spell.comtains_spell_count").append(Component.literal("" + stack.get(ModDataComponents.SPELLS).toArray().length)));
    }
}
