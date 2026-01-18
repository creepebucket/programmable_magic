package org.creepebucket.programmable_magic.spells.old.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class RestartSpell extends BaseControlModLogic {
    @Override
    public String getRegistryName() { return "restart_spell"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.flow_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful", true, "current_spell", spellSequence.getFirstSpell());
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.restart_spell.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.restart_spell.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.EMPTY)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.MODIFIER)); }
}

