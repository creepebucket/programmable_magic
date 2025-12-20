package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellUtils;
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
    public void screenStartupLogic(int x, int y, WandScreen screen) { }

    @Override
    public void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen) { }

    @Override
    public void screenTick(int x, int y, WandScreen screen) {
        screen.isCharging = true;
        screen.chargeTicks += 1;
    }

    @Override
    public void menuLogic(int x, int y, WandMenu menu) { }

    @Override
    public void menuTick(int x, int y, WandMenu menu) { }

    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) { }

    @Override
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) { }

    @Override
    public void adjustWandValues(WandValues values, net.minecraft.world.item.ItemStack pluginStack) { }
}
