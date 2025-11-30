package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ParenSpell extends BaseComputeModLogic{
    public String registryName;

    public ParenSpell(String registryName) {
        this.registryName = registryName;
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful", true);
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.paren.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.paren.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of();
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of();
    }

    public static class RightParenSpell extends ParenSpell{
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

    public static class LeftParenSpell extends ParenSpell{
        public LeftParenSpell() {
            super("compute_lparen");
        }
        private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellEntity");

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

            // 寻找配对的右括号
            SpellItemLogic right = this;
            int paren_count = 1;
            while (true) {
                right = right.getNextSpell();

                // 边界条件
                if (right == null) {
                    ModUtils.sendErrorMessageToPlayer(Component.translatable("error.programmable_magic.no_matching_paren"), player);
                    LOGGER.error("[ProgrammableMagic:SpellEntity] 找不到配对的括号");
                    return Map.of("successful", true);
                }

                if (right instanceof LeftParenSpell) {
                    paren_count++;
                }

                if (right instanceof RightParenSpell) {
                    paren_count--;
                    if (paren_count == 0) return Map.of("successful", true, "value", right);
                }
            }
        }
    }
}
