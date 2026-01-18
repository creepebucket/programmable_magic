package org.creepebucket.programmable_magic.spells.old.compute_mod;

import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;

public abstract class BaseComputeModLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.COMPUTE_MOD;
    }

    @Override
    public boolean isExecutable() { return false; }
}
