package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
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
 * 插件：法术槽视窗与控制按钮（左右翻页/清空/保存）。
 * - screenStartupLogic：添加翻页/清除/保存按钮。
 * - screenRenderLogic：绘制法术槽视窗格与序号。
 * - menuLogic：在菜单中构建可偏移映射的法术槽位。
 */
public class SpellSlots extends BasePlugin{
    private final int tier;
    public SpellSlots() { this(1); }
    public SpellSlots(int tier) { this.tier = Math.max(1, tier); this.pluginName = "spell_slots_t" + this.tier; }

    @Override
    /**
     * 实体 tick：本插件无实体侧行为。
     */
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    public void buildUi(WandMenu menu) {
        menu.ui().addWidget(new WandUiWidgets.SpellSlotsBarWidget(menu));

        var prev = Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_button_prev.png");
        var clear = Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_button_clear.png");
        var next = Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_button_next.png");
        var save = Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_button_save.png");

        menu.ui().addWidget(new ImageButtonWidget(new Coordinate(
                (sw, sh) -> sw / 2 - 8 * WandLayout.visible_spell_slots(sw) - 34,
                (sw, sh) -> sh + WandLayout.SPELL_SLOT_OFFSET - WandLayout.compact_mode_y_offset(sw)
        ), 16, 16, prev, prev, e -> menu.sendMenuData(WandMenu.KEY_SPELL_OFFSET, menu.spellIndexOffset - computeStep(e))));

        menu.ui().addWidget(new ImageButtonWidget(new Coordinate(
                (sw, sh) -> sw / 2 - 8 * WandLayout.visible_spell_slots(sw) - 18,
                (sw, sh) -> sh + WandLayout.SPELL_SLOT_OFFSET - WandLayout.compact_mode_y_offset(sw)
        ), 16, 16, clear, clear, () -> menu.sendMenuData(WandMenu.KEY_CLEAN, true)));

        menu.ui().addWidget(new ImageButtonWidget(new Coordinate(
                (sw, sh) -> sw / 2 + 8 * WandLayout.visible_spell_slots(sw) + 16,
                (sw, sh) -> sh + WandLayout.SPELL_SLOT_OFFSET - WandLayout.compact_mode_y_offset(sw)
        ), 16, 16, next, next, e -> menu.sendMenuData(WandMenu.KEY_SPELL_OFFSET, menu.spellIndexOffset + computeStep(e))));

        menu.ui().addWidget(new ImageButtonWidget(new Coordinate(
                (sw, sh) -> sw / 2 + 8 * WandLayout.visible_spell_slots(sw),
                (sw, sh) -> sh + WandLayout.SPELL_SLOT_OFFSET - WandLayout.compact_mode_y_offset(sw)
        ), 16, 16, save, save, () -> menu.sendMenuData(WandMenu.KEY_SAVE, true)));
    }

    private int computeStep(MouseButtonEvent event) {
        if (event.hasControlDown()) return 100;
        if (event.hasAltDown()) return 25;
        if (event.hasShiftDown()) return 5;
        return 1;
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
     * 数值调整：按等级增加法术槽位数。
     * 等级 = 物品堆叠数量。
     */
    public void adjustWandValues(WandValues values, net.minecraft.world.item.ItemStack pluginStack) {
        values.spellSlots += (int) (64 * Math.pow(2, this.tier));
    }
}
