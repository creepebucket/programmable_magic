package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils.WandValues;

import java.util.List;

public class SpellStorageLogicPlugin extends WandPluginLogic {
    public final int tier;

    public SpellStorageLogicPlugin(int tier) {
        this.tier = tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        appendWhenInstalledHeader(tooltip);
        tooltip.add(Component.literal("+")
                .append(Component.translatable("tooltip.programmable_magic.wand_plugin.spell_storage", tier * 10))
                .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
        values.spellSlots = tier * 10;
    }
}
