package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollbarWidget;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

public class WandScreen extends Screen<WandMenu> {

    public double spellSupplyDeltaYSpeed = 0;
    public double spellSupplyAccurateDeltaY = this.menu.supplySlotDeltaY.get();
    public Double lastFrame = System.nanoTime() / 1e9;
    public Double dt;

    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        /* ===========法术供应段=========== */
        var supplySlotDeltaY = this.menu.supplySlotDeltaY;
        var supplySlotTargetDeltaY = this.menu.supplySlotTargetDeltaY;
        var slotIndex = this.menu.supplySlotsStartIndex;

        // 添加法术供应槽位
        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;

        var dx = 0;
        var dy = 0;
        var categoriesCount = 0;

        // 可以滚动的部分
        for (String key : spells.keySet()) {
            var subCategorySpells = spells.get(key);
            dx = 0;

            // 用于快速跳转到该类别的按钮
            int finalCategoriesCount = categoriesCount;
            this.addWidget(new WandWidgets.WandSubcategoryJumpButton(
                    new Coordinate((w, h) -> 0, (w, h) -> (finalCategoriesCount * h / spells.size())),
                    new Coordinate((w, h) -> 7, (w, h) -> (((finalCategoriesCount + 1) * h / spells.size()) - (finalCategoriesCount * h / spells.size()))),
                    supplySlotTargetDeltaY, -dy + 20, Component.translatable(key), ModUtils.SPELL_COLORS().getOrDefault(key, 0xFFFFFFFF)));

            // 子类别标题
            this.addWidget(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(dx + 8, dy), key, supplySlotDeltaY));

            // 法术
            for (int i = 0; i < subCategorySpells.size(); i++) {
                this.addWidget(new WandWidgets.SpellSupplyWidget(this.menu.slots.get(slotIndex),
                        Coordinate.fromTopLeft(dx % 80 + 8, dy + Math.floorDiv(dx, 80) * 16 + 32), supplySlotDeltaY));
                slotIndex++;

                dx += 16;
            }
            dy = dy + 64 + Math.floorDiv(dx - 16, 80) * 16; // 奇技淫巧和魔法数字的集大成者

            categoriesCount++;
        }

        int finalDy = dy;

        // 滚动交互
        this.addWidget(new WandWidgets.WandSupplyScrollWidget(Coordinate.fromTopLeft(8, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), 16, supplySlotTargetDeltaY));
        // 滚动条
        this.addWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromTopLeft(88, 0), Coordinate.fromBottomLeft(4, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), supplySlotTargetDeltaY, 0xFFFFFFFF, "y", true));

        this.menu.reportScreenSize(this.width, this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // 计算dt
        dt = System.nanoTime() / 1e9 - lastFrame;
        lastFrame = System.nanoTime() / 1e9;

        // 平滑 SupplySlotDeltaY

        var current = spellSupplyAccurateDeltaY;
        var target = (double) menu.supplySlotTargetDeltaY.get();

        // 核心科技, 从chatgpt偷的
        spellSupplyDeltaYSpeed += (target - spellSupplyAccurateDeltaY) * 200 * dt - spellSupplyDeltaYSpeed * 30 * dt;

        double newDy = current + spellSupplyDeltaYSpeed * dt;

        // 过冲检测
        if ((target - current) * (target - newDy) <= 0) {
            newDy = target;
            spellSupplyDeltaYSpeed = 0;
        }
        spellSupplyAccurateDeltaY = newDy;
        menu.supplySlotDeltaY.set((int) newDy);
    }

    private void addWidget(Widget widget) {
        this.menu.widgets.add(widget);
    }
}
