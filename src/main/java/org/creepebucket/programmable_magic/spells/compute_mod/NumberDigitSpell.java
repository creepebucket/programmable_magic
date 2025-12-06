package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class NumberDigitSpell extends BaseComputeModLogic{

    public int DIGIT_NUMBER;

    public NumberDigitSpell(int digit) {
        DIGIT_NUMBER = digit;
    }

    @Override
    public String getRegistryName() {
        return "compute_" + DIGIT_NUMBER;
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 数字法术会自己与相邻的数字组成ValueLiteralSpell

        // 如果当前法术的后一个法术不是数字, 递归调用下一个法术的 run 以确保对于任何法术的 run 调用都返回正确的结果
        if (this.getNextSpell() instanceof NumberDigitSpell) { return this.getNextSpell().run(player, data, spellSequence, modifiers, spellParams); }

        return Map.of("successful", true, "value", combineDigit(0.0, 0));
    }

    public Double combineDigit(Double currentValue, int combinedCount) {
        Double value = currentValue + Math.pow(10, combinedCount) * this.DIGIT_NUMBER;

        SpellItemLogic prevSpell = this.getPrevSpell();
        // 如果上一个法术是数字, 递归调用上一个法术的 combineDigit
        if (prevSpell instanceof NumberDigitSpell) {
            return ((NumberDigitSpell) prevSpell).combineDigit(value, combinedCount + 1);
        }
        return value;
    }

    

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.number_digit.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.number_digit.desc2"),
                Component.translatable("tooltip.programmable_magic.spell.number_digit.desc3")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.EMPTY));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.NUMBER));
    }
}
