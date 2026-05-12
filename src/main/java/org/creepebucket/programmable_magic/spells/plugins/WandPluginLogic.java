package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;
import java.util.Map;

public class WandPluginLogic {
    public void onEntityTick(SpellEntity spellEntity) {
    }

    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams) {
    }

    public void afterSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams) {
    }

    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
    }

    public void appendTooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
    }

    public void appendWhenInstalledHeader(List<Component> tooltip) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.programmable_magic.wand_plugin.when_installed").withStyle(ChatFormatting.GRAY));
    }
}
