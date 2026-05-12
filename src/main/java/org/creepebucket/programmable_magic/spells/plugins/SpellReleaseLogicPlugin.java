package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.ModUtils.WandValues;

import java.util.List;

public class SpellReleaseLogicPlugin extends WandPluginLogic {
    public final int tier;

    public SpellReleaseLogicPlugin(int tier) {
        this.tier = tier;
    }

    @Override
    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
        values.chargeRateW += Math.pow(8, tier - 1) * 1024;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        appendWhenInstalledHeader(tooltip);
        tooltip.add(Component.literal("+")
                .append(Component.translatable("tooltip.programmable_magic.wand_plugin.charge_rate", ModUtils.formattedNumber(Math.pow(8, tier - 1) * 1024)))
                .withStyle(ChatFormatting.BLUE));
    }
}
