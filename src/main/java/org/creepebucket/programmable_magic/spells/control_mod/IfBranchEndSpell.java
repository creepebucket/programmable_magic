package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class IfBranchEndSpell extends BaseControlModLogic {
    @Override
    public String getRegistryName() { return "if_branch_end"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.flow_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful", true);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.if_branch_end.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.if_branch_end.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.EMPTY)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.MODIFIER)); }
}

