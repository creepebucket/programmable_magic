package org.creepebucket.programmable_magic.items.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ModItemExtensions {
    void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt);
}
