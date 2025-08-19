package org.creepebucket.programmable_magic.spells.base_spell;

import org.creepebucket.programmable_magic.spells.SpellItemLogic;

public abstract class BaseSpellEffectLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.BASE_SPELL;
    }
} 