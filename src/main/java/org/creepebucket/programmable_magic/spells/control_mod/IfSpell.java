package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.ModUtils.formatSpellError;
import static org.creepebucket.programmable_magic.spells.SpellUtils.setSpellError;

public class IfSpell extends BaseControlModLogic{

    @Override
    public String getRegistryName() {
        return "if";
    }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.flow_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        SpellItemLogic p = this.getNextSpell();
        IfBranchEndSpell end = null;
        int depth = 0;
        while (p != null) {
            if (p instanceof IfSpell) {
                depth++;
            } else if (p instanceof IfBranchEndSpell e) {
                if (depth == 0) { end = e; break; }
                depth--;
            }
            p = p.getNextSpell();
        }
        if (end == null) {
            int index = SpellUtils.displayIndexOf(spellSequence, this);
            setSpellError(player, data, formatSpellError(
                    Component.translatable("message.programmable_magic.error.kind.syntax"),
                    Component.translatable("message.programmable_magic.error.detail.if_not_pair", index)
            ));
            return Map.of("successful", false, "should_discard", true);
        }

        boolean cond = Boolean.TRUE.equals(spellParams.get(0));
        SpellItemLogic target = cond ? this.getNextSpell() : end;

        return Map.of("successful", true, "current_spell", target);
    }

    

    @Override
    public List<Component> getTooltip() { return List.of(
            Component.translatable("tooltip.programmable_magic.spell.if.desc0"),
            Component.translatable("tooltip.programmable_magic.spell.if.desc1"),
            Component.translatable("tooltip.programmable_magic.spell.if.desc2")
    ); }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.BOOLEAN));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.MODIFIER));
    }
}
