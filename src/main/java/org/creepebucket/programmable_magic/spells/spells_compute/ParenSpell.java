package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

public class ParenSpell {
    public static class LParenSpell extends SpellItemLogic.PairedLeftSpell implements SpellItemLogic.ComputeMod {
        public LParenSpell() {
            this.name = "l_paren";
            this.rightSpellType = RParenSpell.class;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.SUCCESS(this);
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

    public static class RParenSpell extends SpellItemLogic.PairedRightSpell implements SpellItemLogic.ComputeMod {
        public RParenSpell() {
            this.name = "r_paren";
            this.leftSpellType = LParenSpell.class;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.SUCCESS(this);
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
}
