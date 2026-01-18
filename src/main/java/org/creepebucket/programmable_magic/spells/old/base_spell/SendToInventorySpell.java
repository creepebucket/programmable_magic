package org.creepebucket.programmable_magic.spells.old.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class SendToInventorySpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "send_to_inventory"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.entity_interaction"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        ItemStack stack = ((ItemStack) spellParams.get(0)).copy();
        player.getInventory().placeItemBackInInventory(stack);
        return Map.of("successful", true);
    }

    @Override
    public ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new ModUtils.Mana(0.0, 0.0, 0.05, 0.0);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.send_to_inventory.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.send_to_inventory.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.ITEM)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.SPELL)); }
}

