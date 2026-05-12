package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SpellPackerLogicPlugin extends WandPluginLogic {
    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        appendWhenInstalledHeader(tooltip);
        tooltip.add(Component.literal("+")
                .append(Component.translatable("tooltip.programmable_magic.wand_plugin.spell_packer"))
                .withStyle(ChatFormatting.BLUE));
    }
}
