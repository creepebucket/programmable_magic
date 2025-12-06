package org.creepebucket.programmable_magic.spells.base_spell;

import org.creepebucket.programmable_magic.spells.Mana;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;

import java.util.List;

public abstract class BaseBaseSpellLogic extends SpellItemLogic {

    @Override
    public SpellType getSpellType() {
        return SpellType.BASE_SPELL;
    }

    public abstract Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);
}