package org.creepebucket.arcanism.spells.plugins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.arcanism.entities.SpellEntity;
import org.creepebucket.arcanism.spells.api.SpellItemLogic;
import org.creepebucket.arcanism.spells.api.SpellSequence;
import org.creepebucket.arcanism.utils.ModUtils.WandValues;

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
        tooltip.add(Component.translatable("tooltip.arcanism.wand_plugin.when_installed").withStyle(ChatFormatting.GRAY));
    }
}
