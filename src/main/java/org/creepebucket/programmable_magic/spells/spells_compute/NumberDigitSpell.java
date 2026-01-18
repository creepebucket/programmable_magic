package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

public class NumberDigitSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public double digit;

    public NumberDigitSpell(double digit) {
        this.digit = digit;
        this.name = "number_digit_" + digit;
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        // 如果左边是数字, 取左边数的返回值x10 + 本数
        if (this.prev instanceof NumberDigitSpell) {
            List<Object> result = this.prev.run(caster, spellSequence, paramsList, spellEntity).returnValue;
            result.set(0, ((double) result.get(0)) * 10 + this.digit); // 绝对不会null
            return ExecutionResult.RETURNED(this, result, List.of(SpellValueType.NUMBER, SpellValueType.SPELL));
        }

        return ExecutionResult.RETURNED(this, List.of(this.digit, this), List.of(SpellValueType.NUMBER, SpellValueType.SPELL));
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }
}
