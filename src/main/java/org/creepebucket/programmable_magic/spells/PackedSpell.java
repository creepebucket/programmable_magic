package org.creepebucket.programmable_magic.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.api.ModItemExtensions;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.jspecify.annotations.Nullable;

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
    public void append_tooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        tooltip.add(Component.translatable("tooltip.programmable_magic.packed_spell.auther").append(Component.literal(stack.get(ModDataComponents.AUTHER)).withStyle(ChatFormatting.GREEN)));
        tooltip.add(Component.translatable("tooltip.programmable_magic.packed_spell.description"));
        tooltip.add(Component.literal("    ").append(Component.literal(stack.get(ModDataComponents.DESCRIPTION)).withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.translatable("tooltip.programmable_magic.packed_spell.comtains_spell_count").append(Component.literal("" + stack.get(ModDataComponents.SPELLS).toArray().length)));
    }
}
