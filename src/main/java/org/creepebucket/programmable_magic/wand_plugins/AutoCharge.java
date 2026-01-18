package org.creepebucket.programmable_magic.wand_plugins;

import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandUiWidgets;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;
import org.creepebucket.programmable_magic.ModUtils.WandValues;

import java.util.List;

/**
 * 插件：自动充能（在按住充能时每tick额外+N）。
 */
public class AutoCharge extends BasePlugin {

    public AutoCharge() { this.pluginName = "auto_charge"; }

    @Override
    public void onEntityTick(SpellEntity spellEntity) { }

    @Override
    public void buildUi(WandMenu menu) {
        menu.ui().addWidget(new WandUiWidgets.AutoChargeWidget(menu));
    }

    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) { }

    @Override
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) { }

    @Override
    public void adjustWandValues(WandValues values, net.minecraft.world.item.ItemStack pluginStack) { }
}
