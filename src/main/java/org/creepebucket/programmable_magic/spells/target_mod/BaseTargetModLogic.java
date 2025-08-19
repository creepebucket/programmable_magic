package org.creepebucket.programmable_magic.spells.target_mod;

import org.creepebucket.programmable_magic.spells.SpellItemLogic;

public abstract class BaseTargetModLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.TARGET_MOD;
    }
} 