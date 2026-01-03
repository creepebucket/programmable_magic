package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
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

/**
 * 插件：法术释放（按下释放按钮进入充能，松开后发送释放数据包）。
 * - screenStartupLogic：添加发射按钮并进入充能态。
 * - screenRenderLogic：在充能时绘制能量读数。
 */
public class SpellRelease extends BasePlugin{
    private final int tier;
    public SpellRelease() { this(1); }
    public SpellRelease(int tier) { this.tier = Math.max(1, tier); this.pluginName = "spell_release_t" + this.tier; }

    @Override
    /**
     * 实体 tick：本插件无实体侧行为。
     */
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    /**
     * 屏幕初始化：添加发射按钮，按下进入充能状态。
     */
    public void screenStartupLogic(int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = sw / 2;

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        int compactModeYOffset = spellSlotCount <= 16 ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        // 法术释放
        var releaseTex = Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_release.png");
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X - 112 / 2, sh - 100 - compactModeYOffset, 112, 16, releaseTex, releaseTex, () -> {
            screen.isCharging = true; // 进入充能态，具体每tick自增由 Screen.containerTick 执行
        }));
    }

    @Override
    /**
     * 屏幕渲染：若处于充能态，绘制实时能量数值。
     */
    public void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = screen.width / 2 - 1 - screen.getGuiLeft();
        int BOTTOM_Y = sh - screen.getGuiTop();

        // 发射按钮能量显示
        if (screen.isCharging) guiGraphics.drawString(screen.getFont(),
                ModUtils.FormattedManaString(((double) screen.chargeTicks / 20.0) * (screen.getMenuChargeRate() / 1000.0)),
                CENTER_X - 20, BOTTOM_Y - 94, 0xFFFFFFFF);
    }

    @Override
    public void screenTick(int x, int y, WandScreen screen) {
        // 本插件不做额外 tick 行为
    }

    @Override
    /**
     * 菜单布局：本插件不参与菜单侧布局。
     */
    public void menuLogic(int x, int y, WandMenu menu) {

    }

    @Override
    /**
     * 菜单 tick：本插件无菜单侧持续行为。
     */
    public void menuTick(int x, int y, WandMenu menu) {

    }

    @Override
    /**
     * 执行前：不更改法术参数。
     */
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    /**
     * 执行后：不更改执行结果。
     */
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    /**
     * 数值调整：按等级提升充能功率（W）。
     * 等级 = 物品堆叠数量。
     */
    public void adjustWandValues(WandValues values, net.minecraft.world.item.ItemStack pluginStack) {
        values.chargeRateW += 4000 * Math.pow(8, this.tier);
    }
}
