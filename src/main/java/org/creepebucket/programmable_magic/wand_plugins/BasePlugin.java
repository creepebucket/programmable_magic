package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.List;

public abstract class BasePlugin {
    public String name;

    public String getRegistryName() {
        return "wand_plugin_".concat(name);
    }

    public abstract void onEntityTick(SpellEntity spellEntity);
    public abstract void screenStartupLogic(int x, int y, WandScreen screen);
    public abstract void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen);
    public abstract void menuLogic(int x, int y, WandMenu menu);
    public abstract void menuTick(int x, int y, WandMenu menu);
    public abstract void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);
    public abstract void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);
}
