package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandScreen extends Screen<WandMenu> {

    public static double dt;
    public double spellSupplyDeltaYSpeed = 0;
    public double spellSupplyAccurateDeltaY = this.menu.supplySlotDeltaY.get();
    public List<WandWidgets.SpellStorageWidget> storageSlots = new ArrayList<>();
    public Double lastFrame = System.nanoTime() / 1e9;

    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        /* ===========法术供应段=========== */
        var supplySlotDeltaY = this.menu.supplySlotDeltaY;
        var supplySlotTargetDeltaY = this.menu.supplySlotTargetDeltaY;
        var spellSlotTargetDeltaX = this.menu.spellSlotTargetDeltaX;
        var slotIndex = this.menu.supplySlotsStartIndex;

        // 添加法术供应槽位
        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;

        var dx = 0;
        var dy = 0;
        var categoriesCount = 0;

        // 背景
        addWidget(new RectangleWidget(Coordinate.fromTopLeft(7, 0), Coordinate.fromBottomLeft(81, 0), 0x80000000));

        // 可以滚动的部分
        for (String key : spells.keySet()) {
            var subCategorySpells = spells.get(key);
            dx = 0;

            // 用于快速跳转到该类别的按钮
            int finalCategoriesCount = categoriesCount;
            addWidget(new WandWidgets.WandSubcategoryJumpButton(
                    new Coordinate((w, h) -> 0, (w, h) -> (finalCategoriesCount * h / spells.size())),
                    new Coordinate((w, h) -> 7, (w, h) -> (((finalCategoriesCount + 1) * h / spells.size()) - (finalCategoriesCount * h / spells.size()))),
                    supplySlotTargetDeltaY, -dy + 20, Component.translatable(key), ModUtils.SPELL_COLORS().getOrDefault(key, 0xFFFFFFFF)));

            // 子类别标题
            addWidget(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(dx + 8, dy), key, supplySlotDeltaY));

            // 法术
            for (int i = 0; i < subCategorySpells.size(); i++) {
                addWidget(new WandWidgets.SpellSupplyWidget(this.menu.slots.get(slotIndex),
                        Coordinate.fromTopLeft(dx % 80 + 8, dy + Math.floorDiv(dx, 80) * 16 + 32), supplySlotDeltaY));
                slotIndex++;

                dx += 16;
            }
            dy = dy + 64 + Math.floorDiv(dx - 16, 80) * 16; // 奇技淫巧和魔法数字的集大成者

            categoriesCount++;
        }

        int finalDy = dy;

        // 滚动交互
        addWidget(new ScrollRegionWidget(Coordinate.fromTopLeft(8, 0), Coordinate.fromTopLeft(80, 999),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), 16, supplySlotTargetDeltaY));
        // 滚动条
        addWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromTopLeft(88, 0), Coordinate.fromBottomLeft(4, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), supplySlotTargetDeltaY, 0xFFFFFFFF, "y", true));

        /* ===========法术储存段=========== */
        var spellCountCanFit = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 16 - 8;

        for (int i = 0; i < spellCountCanFit; i++) {
            var pos = new Coordinate((w, h) -> (w - spellCountCanFit * 16) / 2 + 64, (w, h) -> h - 115);

            var storage = new WandWidgets.SpellStorageWidget(menu.spellStoreSlots, pos, i, menu.storedSpellsEditHook, menu.clearSpellsHook, storageSlots, spellSlotTargetDeltaX);
            addWidget(storage);
            storageSlots.add(storage);
        }

        var targetMin = -1002 * 16 + spellCountCanFit * 16;

        // 两边遮挡
        addWidget(new ImageButtonWidget(Coordinate.fromBottomLeft(94, -115), Coordinate.fromTopLeft(32, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_left.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_left.png"),
                () -> {
                    spellSlotTargetDeltaX.set(Math.clamp(spellSlotTargetDeltaX.get() + 80, targetMin, 0));
                }, Component.translatable("gui.programmable_magic.wand.spells.left_shift")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-30, -115), Coordinate.fromTopLeft(32, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_right.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_right.png"),
                () -> {
                    spellSlotTargetDeltaX.set(Math.clamp(spellSlotTargetDeltaX.get() - 80, targetMin, 0));
                }, Component.translatable("gui.programmable_magic.wand.spells.right_shift")));

        // 滚动条
        addWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromBottomLeft(96, -112 + 15), Coordinate.fromTopRight(-96, 4),
                Coordinate.fromTopLeft(targetMin, 0), spellSlotTargetDeltaX, -1, "X", false));

        // 滚轮区域
        addWidget(new ScrollRegionWidget(Coordinate.fromBottomLeft(94, -113), Coordinate.fromTopLeft(999, 16), Coordinate.fromTopLeft(targetMin, 0), 80, spellSlotTargetDeltaX));

        /* ===========玩家物品栏=========== */

        List<Slot> inventorySlots = menu.hotbarSlots;
        inventorySlots.addAll(menu.backpackSlots);

        for (int i = 0; i < 36; i++) {
            addWidget(new SlotWidget(inventorySlots.get(i), Coordinate.fromBottomLeft(97 + 18 * (i % 9), 18 * (i / 9) - 72)));
            addWidget(new RectangleWidget(Coordinate.fromBottomLeft(97 + 18 * (i % 9), 18 * (i / 9) - 72), Coordinate.fromTopLeft(16, 16), i < 9 ? 0x80606060 : -2147483648));
        }

        /* ===========法术调试器=========== */

        addWidget(new TextureWidget(Coordinate.fromBottomRight(-16, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger.png"), Coordinate.fromTopLeft(16, 16)));

        // 调试
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_step.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_step.png"),
                () -> {
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_step")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 2), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_tick.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_tick.png"),
                () -> {
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_tick")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 3), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_resume.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_resume.png"),
                () -> {
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_resume")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 4), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_pause.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_pause.png"),
                () -> {
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_pause")));

        // 编辑
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-32 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/right_shift.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/right_shift.png"),
                () -> {
                    menu.storedSpellsEditHook.trigger(-1, false);
                    for (WandWidgets.SpellStorageWidget widget : storageSlots) widget.delta2X -= 16;
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_right_shift")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-48 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export.png"),
                () -> {
                    Minecraft.getInstance().keyboardHandler.setClipboard(ModUtils.serializeSpells(menu.storedSpells));
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_export")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-64 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/trashcan.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/trashcan.png"),
                () -> {
                    for (WandWidgets.SpellStorageWidget widget : storageSlots)
                        widget.acc2 = Minecraft.getInstance().getWindow().getGuiScaledWidth() * 1.2;
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_delete")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-80 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import.png"),
                () -> {
                    menu.importSpellsHook.trigger(Minecraft.getInstance().keyboardHandler.getClipboard());
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_import")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-96 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/left_shift.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/left_shift.png"),
                () -> {
                    menu.storedSpellsEditHook.trigger(-1, true);
                    for (WandWidgets.SpellStorageWidget widget : storageSlots) widget.delta2X += 16;
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_left_shift")));

        /* ===========边界装饰=========== */

        // 法术储存段
        addWidget(new RectangleWidget(Coordinate.fromTopLeft(93, 0), Coordinate.fromBottomLeft(2, 0), -1));

        // 玩家物品栏
        addWidget(new RectangleWidget(Coordinate.fromBottomLeft(95, -76 - 16), Coordinate.fromTopRight(0, 2), -1));

        addWidget(new TextureWidget(Coordinate.fromBottomLeft(95, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/backpack.png"), Coordinate.fromTopLeft(16, 16)));
        addWidget(new TextureWidget(Coordinate.fromBottomLeft(98 + 9 * 18 - 49, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/slant_end_bar_up.png"), Coordinate.fromTopLeft(48, 16)));
        addWidget(new TextWidget(Coordinate.fromBottomLeft(95 + 16, -76 - 10), Component.translatable("gui.programmable_magic.wand.inventory"), -1));

        addWidget(new RectangleWidget(Coordinate.fromBottomLeft(95 + 18 * 9 + 2, -76 - 16), Coordinate.fromTopLeft(2, 92), -1));

        // 法术调试器
        addWidget(new RectangleWidget(Coordinate.fromBottomRight(-18, -92), Coordinate.fromTopLeft(2, 92), -1));
        addWidget(new RectangleWidget(Coordinate.fromBottomRight(-9, -92 + 16 + 5), Coordinate.fromTopLeft(2, 2), -1));


        // addWidget(new MouseCursorWidget());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // 计算dt
        dt = System.nanoTime() / 1e9 - lastFrame;
        lastFrame = System.nanoTime() / 1e9;

        /* ========================================== */

        var MaGiCaL_CoNsTaNt_1 = 200;
        var MaGiCaL_CoNsTaNt_2 = 30;

        /* ========================================== */

        // 平滑 SupplySlotDeltaY

        var current = spellSupplyAccurateDeltaY;
        var target = (double) menu.supplySlotTargetDeltaY.get();

        // 核心科技, 从chatgpt偷的
        spellSupplyDeltaYSpeed += (target - spellSupplyAccurateDeltaY) * MaGiCaL_CoNsTaNt_1 * dt - spellSupplyDeltaYSpeed * MaGiCaL_CoNsTaNt_2 * dt;

        double newDy = current + spellSupplyDeltaYSpeed * dt;

        // 过冲检测
        if (target > current ^ target > newDy) {
            newDy = target;
            spellSupplyDeltaYSpeed = 0;
        }
        spellSupplyAccurateDeltaY = newDy;
        menu.supplySlotDeltaY.set((int) newDy);
    }

    @Override
    public void resize(int width, int height) {
        // 动态地在大小改变时重建控件
        this.menu.widgets = new ArrayList<>();
        super.resize(width, height);
    }
}
