package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellRelease extends BasePlugin{
    public SpellRelease() {
        this.name = "spell_release";
    }

    @Override
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    public void screenStartupLogic(int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = sw / 2;

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        int compactModeYOffset = spellSlotCount <= 16 ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        // 法术释放
        var releaseTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_release.png");
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X - 112 / 2, sh - 100 - compactModeYOffset, 112, 16, releaseTex, releaseTex, () -> {
            screen.isCharging = true;
            screen.chargeTicks = 0; // 充能开始时重置到0，由 containerTick 每tick自增
        }));
    }

    @Override
    public void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = screen.width / 2 - 1 - screen.getGuiLeft();
        int BOTTOM_Y = sh - screen.getGuiTop();

        // 发射按钮能量显示
        if (screen.isCharging) guiGraphics.drawString(screen.getFont(),
                ModUtils.FormattedManaString(((double) screen.chargeTicks / 20.0) * (screen.chargeRate / 1000.0)),
                CENTER_X - 20, BOTTOM_Y - 94, 0xFFFFFFFF);
    }

    @Override
    public void menuLogic(int x, int y, WandMenu menu) {

    }

    @Override
    public void menuTick(int x, int y, WandMenu menu) {

    }

    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }
}
