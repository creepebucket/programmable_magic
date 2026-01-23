package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class NumberDigitSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public int digit;

    public NumberDigitSpell(int digit) {
        this.digit = digit;
        this.name = "number_digit_" + digit;
        this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.numbers");

        this.outputTypes = List.of(List.of(SpellValueType.NUMBER));
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        // 如果左边是数字, 取左边数的返回值x10 + 本数
        if (this.prev instanceof NumberDigitSpell) {
            List<Object> result = this.prev.run(caster, spellSequence, paramsList, spellEntity).returnValue;
            result.set(0, ((double) result.get(0)) * 10 + this.digit); // 绝对不会null
            return ExecutionResult.RETURNED(this, result, List.of(SpellValueType.NUMBER, SpellValueType.SPELL));
        }
        //

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

    // 各个数字

    public static class NumberDigit0 extends NumberDigitSpell {
        public NumberDigit0() {
            super(0);
        }
    }

    public static class NumberDigit1 extends NumberDigitSpell {
        public NumberDigit1() {
            super(1);
        }
    }

    public static class NumberDigit2 extends NumberDigitSpell {
        public NumberDigit2() {
            super(2);
        }
    }

    public static class NumberDigit3 extends NumberDigitSpell {
        public NumberDigit3() {
            super(3);
        }
    }

    public static class NumberDigit4 extends NumberDigitSpell {
        public NumberDigit4() {
            super(4);
        }
    }

    public static class NumberDigit5 extends NumberDigitSpell {
        public NumberDigit5() {
            super(5);
        }
    }

    public static class NumberDigit6 extends NumberDigitSpell {
        public NumberDigit6() {
            super(6);
        }
    }

    public static class NumberDigit7 extends NumberDigitSpell {
        public NumberDigit7() {
            super(7);
        }
    }

    public static class NumberDigit8 extends NumberDigitSpell {
        public NumberDigit8() {
            super(8);
        }
    }

    public static class NumberDigit9 extends NumberDigitSpell {
        public NumberDigit9() {
            super(9);
        }
    }
}
