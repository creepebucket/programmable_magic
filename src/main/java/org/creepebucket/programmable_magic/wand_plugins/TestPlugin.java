package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;
import java.util.Map;

public class TestPlugin extends BasePlugin {
    @Override
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    public void buildUi(WandMenu menu) {

    }

    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams) {

    }

    @Override
    public void afterSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams) {

    }

    @Override
    public void adjustWandValues(ModUtils.WandValues values, ItemStack pluginStack) {

    }

    @Override
    public Component function() {
        return Component.literal("mojang");
    }
}
