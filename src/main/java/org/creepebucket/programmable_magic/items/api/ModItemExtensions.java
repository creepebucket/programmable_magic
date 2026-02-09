package org.creepebucket.programmable_magic.items.api;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public interface ModItemExtensions {
    void append_tooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt);
}
