package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;
import java.util.Map;

public class WandPluginLogic {
    public void onEntityTick(SpellEntity spellEntity) {
    }

    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams) {
    }

    public void afterSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams) {
    }

    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
    }
}
