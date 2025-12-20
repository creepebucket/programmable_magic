package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.MathUtils;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellUtils;

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
    /**
     * 屏幕初始化：添加法术槽控制按钮（上一页/清空/下一页/保存）。
     */
    public void screenStartupLogic(int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = sw / 2;

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        int compactModeYOffset = spellSlotCount <= 16 ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        // 法术控制
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X - 8 * spellSlotCount - 34, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_prev.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_prev.png"), () -> {
            screen.updateSpellIndex(-screen.computeStep());
            screen.sendMenuData(WandMenu.KEY_SPELL_OFFSET, screen.spellIndexOffset);
        }));
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X - 8 * spellSlotCount - 18, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_clear.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_clear.png"), () -> {
            screen.sendMenuData(WandMenu.KEY_CLEAN, true);
        }));
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X + 8 * spellSlotCount + 16, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_next.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_next.png"), () -> {
            screen.updateSpellIndex(screen.computeStep());
            screen.sendMenuData(WandMenu.KEY_SPELL_OFFSET, screen.spellIndexOffset);
        }));
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X + 8 * spellSlotCount, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_save.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_save.png"), () -> {
            // 将当前屏幕中的法术存入“隐藏”数据组件，由服务端菜单处理
            screen.sendMenuData(WandMenu.KEY_SAVE, true);
        }));
    }

    @Override
    /**
     * 屏幕渲染：按当前偏移绘制一行法术槽与其编号。
     */
    public void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = screen.width / 2 - 1 - screen.getGuiLeft();
        int BOTTOM_Y = sh - screen.getGuiTop();


        // 法术槽
        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        boolean compactMode = spellSlotCount <= 16;
        int compactModeYOffset = compactMode ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        for (int i = 0; i < spellSlotCount; i++) screen.drawSpellSlot(guiGraphics, screen.spellIndexOffset + i, CENTER_X - spellSlotCount * 8 + i * 16, BOTTOM_Y + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset);
    }

    @Override
    public void screenTick(int x, int y, WandScreen screen) {
        // 本插件不做额外 tick 行为
    }

    @Override
    /**
     * 菜单布局：创建与屏幕长度相适配的一排 OffsetSlot（映射 wandInv）。
     */
    public void menuLogic(int x, int y, WandMenu menu) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        boolean compactMode = spellSlotCount <= 16;

        int centerX = sw / 2;
        menu.spellStartIndex = menu.slots.size();
        for (int i = 0; i < spellSlotCount; i++) menu.spellSlots.add(menu.addOffsetSlotConverted(menu.wandInv, i, centerX - spellSlotCount * 8 + i * 16 - 1, sh + MathUtils.SPELL_SLOT_OFFSET - (compactMode ? 18 : 0)));
        menu.spellEndIndex = menu.slots.size();
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
     * 数值调整：按等级增加法术槽位数。
     * 等级 = 物品堆叠数量。
     */
    public void adjustWandValues(WandValues values, net.minecraft.world.item.ItemStack pluginStack) {
        values.spellSlots += (int) (64 * Math.pow(2, this.tier));
    }
}
