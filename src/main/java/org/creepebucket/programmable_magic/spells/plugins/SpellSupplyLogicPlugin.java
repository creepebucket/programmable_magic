package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.utils.ModUtils.WandValues;

import java.util.List;

public class SpellSupplyLogicPlugin extends WandPluginLogic {
    public final int tier;

    public SpellSupplyLogicPlugin(int tier) {
        this.tier = tier;
    }

    @Override
    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
        values.manaMult = Math.pow(0.95, tier - 1);
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        appendWhenInstalledHeader(tooltip);
        tooltip.add(Component.literal("+")
                .append(Component.translatable("tooltip.programmable_magic.wand_plugin.mana_mult", String.format("%.2f", Math.pow(0.95, tier - 1))))
                .withStyle(ChatFormatting.BLUE));
    }
}
