package org.creepebucket.arcanism.spells.spells_compute;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.arcanism.entities.SpellEntity;
import org.creepebucket.arcanism.spells.api.ExecutionResult;
import org.creepebucket.arcanism.spells.api.SpellItemLogic;
import org.creepebucket.arcanism.spells.api.SpellSequence;
import org.creepebucket.arcanism.utils.Mana;

import java.util.List;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class CommaSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {
    public CommaSpell() {
        this.name = "comma";
        this.subCategory = "spell." + MODID + ".subcategory.structure";
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
    public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new Mana();
    }
}
