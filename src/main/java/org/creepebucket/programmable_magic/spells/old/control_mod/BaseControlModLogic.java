package org.creepebucket.programmable_magic.spells.old.control_mod;

import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;

public abstract class BaseControlModLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.CONTROL_MOD;
    }
}