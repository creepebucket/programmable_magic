package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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

public class SpellSlots extends BasePlugin{
    public SpellSlots() { this.name = "spell_slots"; }

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

        // 法术控制
        screen.addRenderableWidget(new WandScreen.ImageButtonWidget(CENTER_X - 8 * spellSlotCount - 34, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_prev.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_prev.png"), () -> {
            int step = screen.computeStep();
            screen.updateSpellIndex(-step);
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
            int step = screen.computeStep();
            screen.updateSpellIndex(step);
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
    public void menuTick(int x, int y, WandMenu menu) {

    }

    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }
}
