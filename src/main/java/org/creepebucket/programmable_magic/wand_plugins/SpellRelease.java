package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.wand.WandLayout;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandUiWidgets;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;

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
    public void buildUi(WandMenu menu) {
        var releaseTex = Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_release.png");

        menu.ui().addWidget(new ImageButtonWidget(new Coordinate(
                (sw, sh) -> WandUiWidgets.release_button_screen_x(sw),
                (sw, sh) -> WandUiWidgets.release_button_screen_y(sw, sh)
        ), WandUiWidgets.RELEASE_BUTTON_WIDTH, WandUiWidgets.RELEASE_BUTTON_HEIGHT, releaseTex, releaseTex, () -> menu.isCharging = true));

        menu.ui().addWidget(new TextWidget(new Coordinate(
                (sw, sh) -> sw / 2 - 20,
                (sw, sh) -> sh - 94 - WandLayout.compact_mode_y_offset(sw)
        ), () -> menu.isCharging
                ? ModUtils.FormattedManaString(((double) menu.chargeTicks / 20.0) * (menu.getChargeRate() / 1000.0))
                : "", () -> 0xFFFFFFFF));

        menu.ui().addWidget(new WandUiWidgets.SpellReleaseOnMouseReleasedWidget(menu));
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
