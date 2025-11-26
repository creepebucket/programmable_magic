package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RightParenSpell extends ParenSpell{
    public RightParenSpell() { super("compute_rparen"); }
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellEntity");

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

        // 寻找配对的左括号
        SpellItemLogic left = this;
        int paren_count = 1;
        while (true) {
            left = left.getPrevSpell();

            // 边界条件
            if (left == null) {
                ModUtils.sendErrorMessageToPlayer(Component.translatable("error.programmable_magic.no_matching_paren"), player);
                LOGGER.error("[ProgrammableMagic:SpellEntity] 找不到配对的括号");
                return Map.of("successful", true);
            }

            if (left instanceof RightParenSpell) {
                paren_count++;
            }

            if (left instanceof LeftParenSpell) {
                paren_count--;
                if (paren_count == 0) return Map.of("successful", true, "value", left);
            }
        }
    }
}
