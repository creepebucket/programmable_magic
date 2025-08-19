package org.creepebucket.programmable_magic.spells.adjust_mod;

import org.creepebucket.programmable_magic.spells.SpellItemLogic;

public abstract class BaseAdjustModLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.ADJUST_MOD;
    }
} 