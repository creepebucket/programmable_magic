package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ParenSpell;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.ModUtils.sendErrorMessageToPlayer;

public class IfSpell extends BaseControlModLogic{

    @Override
    public String getRegistryName() {
        return "if";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 如果条件为真, 则执行if之后的一个法术
        SpellItemLogic pointer = this.getNextSpell();
        SpellItemLogic seq1start = null;
        SpellItemLogic seq2start = null;
        boolean flag = false;

        while (true) {
            if (pointer == null) {
                // 未找到配对
                sendErrorMessageToPlayer(Component.translatable("programmable_magic.error.if_not_pair"), player);
                return Map.of("successful", true);
            } else if (pointer instanceof ParenSpell.LeftParenSpell) {
                if (flag) {
                    // 找到配对
                    seq2start = pointer;
                    break;
                } else {
                    // 找到配对
                    seq1start = pointer;
                    flag = true;
                }
            }
            pointer = pointer.getNextSpell();
        }

        return Map.of(
                "successful", true,
                "current_spell", (boolean) spellParams.get(0) ? seq1start.getNextSpell() : seq2start.getNextSpell()
        );
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
