package org.creepebucket.programmable_magic.spells.compute_mod;

import org.creepebucket.programmable_magic.spells.SpellItemLogic;

public abstract class BaseComputeModLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.COMPUTE_MOD;
    }
}

