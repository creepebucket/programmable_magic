package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

import java.util.List;

public abstract class BaseComputeModLogic extends SpellItemLogic {
    @Override
    public SpellType getSpellType() {
        return SpellType.COMPUTE_MOD;
    }
}
