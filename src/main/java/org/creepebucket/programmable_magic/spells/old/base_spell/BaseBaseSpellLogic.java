package org.creepebucket.programmable_magic.spells.old.base_spell;

import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;

import java.util.List;

public abstract class BaseBaseSpellLogic extends SpellItemLogic {

    @Override
    public SpellType getSpellType() {
        return SpellType.BASE_SPELL;
    }

    public abstract ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);
}