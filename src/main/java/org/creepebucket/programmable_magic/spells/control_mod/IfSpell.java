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
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.flow_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 新语义：仅使用一个括号块作为真分支；为假则跳过该括号块
        // 查找 if 右侧最近的左括号
        SpellItemLogic p = this.getNextSpell();
        ParenSpell.LeftParenSpell left = null;
        while (p != null) {
            if (p instanceof ParenSpell.LeftParenSpell l) { left = l; break; }
            p = p.getNextSpell();
        }
        if (left == null) {
            sendErrorMessageToPlayer(Component.translatable("programmable_magic.error.if_not_pair"), player);
            return Map.of("successful", true);
        }

        // 寻找与之配对的右括号
        SpellItemLogic q = left.getNextSpell();
        int depth = 1;
        ParenSpell.RightParenSpell right = null;
        while (q != null) {
            if (q instanceof ParenSpell.LeftParenSpell) depth++;
            else if (q instanceof ParenSpell.RightParenSpell r) {
                depth--;
                if (depth == 0) { right = r; break; }
            }
            q = q.getNextSpell();
        }
        if (right == null) {
            sendErrorMessageToPlayer(Component.translatable("programmable_magic.error.if_not_pair"), player);
            return Map.of("successful", true);
        }

        boolean cond = Boolean.TRUE.equals(spellParams.get(0));
        SpellItemLogic target = cond ? left.getNextSpell() : right.getNextSpell();

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
