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

public class LoopBreakSpell extends BaseControlModLogic{
    @Override
    public String getRegistryName() {
        return "loop_break";
    }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.loop_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 跳出循环
        SpellItemLogic pointer = this.getNextSpell();
        while (true) {
            if (pointer == null) {
                // 未找到配对
                int index = SpellUtils.displayIndexOf(spellSequence, this);
                setSpellError(player, data, formatSpellError(
                        Component.translatable("message.programmable_magic.error.kind.syntax"),
                        Component.translatable("message.programmable_magic.error.detail.loop_not_pair", index)
                ));
                return Map.of("successful", false, "should_discard", true);
            } else if (pointer instanceof LoopEndSpell) {
                break;
            }
            pointer = pointer.getNextSpell();
        }

        SpellItemLogic next = pointer.getNextSpell();
        return (next == null)
                ? Map.of("successful", true, "should_discard", true)
                : Map.of("successful", true, "current_spell", next);
    }

    

    @Override
    public List<Component> getTooltip() { return List.of(
            Component.translatable("tooltip.programmable_magic.spell.loop_break.desc1"),
            Component.translatable("tooltip.programmable_magic.spell.loop_break.desc2")
    ); }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.EMPTY));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.MODIFIER));
    }
}
