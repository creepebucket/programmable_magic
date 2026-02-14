package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollRegionWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollbarWidget;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.gui.wand.WandSlots;
import org.creepebucket.programmable_magic.gui.wand.WandWidgets;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.hsvToArgb;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellSupplyPlugin extends BasePlugin {
    public int tier;

    public SpellSupplyPlugin(int tier) {
        this.tier = tier;
        pluginName = "spell_supply_t" + tier;
    }

    @Override
    public void onAdd(WandScreen screen) {
        var slotIndex = screen.getMenu().supplySlotsStartIndex;

        var supplySlotDeltaY = screen.getMenu().supplySlotDeltaY;
        var supplySlotTargetDeltaY = screen.getMenu().supplySlotTargetDeltaY;

        // 添加法术供应槽位
        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;

        var dx = 0;
        var dy = 0;
        var categoriesCount = 0;

        // 由于背景需要在最后时候绘制, 所以创建一个组件缓冲区
        List<Widget> buffer = new ArrayList<>();

        // 可以滚动的部分
        for (String key : spells.keySet()) {
            var subCategorySpells = spells.get(key);
            dx = 0;

            // 用于快速跳转到该类别的按钮
            int finalCategoriesCount = categoriesCount;
            buffer.add(new WandWidgets.WandSubcategoryJumpButton(
                    new Coordinate((w, h) -> 0, (w, h) -> (finalCategoriesCount * h / (spells.size() + 1))),
                    new Coordinate((w, h) -> 7, (w, h) -> (((finalCategoriesCount + 1) * h / (spells.size() + 1)) - (finalCategoriesCount * h / (spells.size() + 1)))),
                    supplySlotTargetDeltaY, -dy + 20, Component.translatable(key), ModUtils.SPELL_COLORS().getOrDefault(key, 0xFFFFFFFF)));

            // 子类别标题
            buffer.add(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(dx + 8, dy), key, supplySlotDeltaY));

            // 法术
            for (int i = 0; i < subCategorySpells.size(); i++) {
                buffer.add(new WandWidgets.SpellSupplyWidget(screen.getMenu().slots.get(slotIndex),
                        Coordinate.fromTopLeft(dx % 80 + 8, dy + Math.floorDiv(dx, 80) * 16 + 32), supplySlotDeltaY));
                slotIndex++;

                dx += 16;
            }
            dy = dy + 64 + Math.floorDiv(dx - 16, 80) * 16; // 奇技淫巧和魔法数字的集大成者

            categoriesCount++;
        }

        dx = 0;
        // 自定义法术供应栏
        buffer.add(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(8, dy), "spell." + MODID + ".subcategory.custom", supplySlotDeltaY));
        buffer.add(new WandWidgets.WandSubcategoryJumpButton(
                new Coordinate((w, h) -> 0, (w, h) -> (h - 1 - h / (spells.size() + 1))),
                new Coordinate((w, h) -> 7, (w, h) -> 1 + h / (spells.size() + 1)),
                supplySlotTargetDeltaY, -dy + 20, Component.translatable("spell." + MODID + ".subcategory.custom"), ModUtils.SPELL_COLORS().getOrDefault("spell." + MODID + ".subcategory.custom", 0xFFFFFFFF)));

        for (WandSlots.CustomSupplySlot slot : screen.getMenu().customSupplySlots) {
            buffer.add(new WandWidgets.SpellSupplyWidget(slot, Coordinate.fromTopLeft(dx % 80 + 8, Math.floorDiv(dx, 80) * 16 + dy + 32), supplySlotDeltaY));
            dx += 16;
        }

        int finalDy = dy + 160 + 64;

        // 背景
        screen.addWidget(new WandWidgets.DyRectangleWidget(Coordinate.fromTopLeft(7, 0), Coordinate.fromTopLeft(81, finalDy - 30), 0x80000000, supplySlotDeltaY));

        // 添加缓冲区widget
        for (Widget widget : buffer) screen.addWidget(widget);

        // 自定义供应的锁定按钮
        screen.lockButton = new WandWidgets.DySelectableImageButtonWidget(Coordinate.fromTopLeft(7, finalDy - 28), Coordinate.fromTopLeft(80, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/wand_lock_button.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/wand_unlock_button.png"),
                Component.translatable("gui.programmable_magic.wand.spells.unlock_custom"), supplySlotDeltaY);
        screen.addWidget(screen.lockButton);

        // 滚动交互
        screen.addWidget(new ScrollRegionWidget(Coordinate.fromTopLeft(8, 0), Coordinate.fromTopLeft(80, 999),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), 16, supplySlotTargetDeltaY));
        // 滚动条
        screen.addWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromTopLeft(88, 0), Coordinate.fromBottomLeft(4, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), supplySlotTargetDeltaY, 0xFFFFFFFF, "y", true));

        screen.addWidget(new RectangleWidget(Coordinate.fromTopLeft(93, 0), Coordinate.fromBottomLeft(2, 0), -1));

    }

    @Override
    public void render(WandScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 同步 customSupplySlotSupplyMode
        screen.getMenu().customSupplySlotSupplyMode.set(!screen.lockButton.isSelected);
    }

    @Override
    public void adjustWandValues(ModUtils.WandValues values, ItemStack pluginStack) {
        values.manaMult = Math.pow(0.95, tier - 1);
    }

    @Override
    public Component function() {
        return Component.translatable("gui.programmable_magic.wand.plugin.mana_mult_mult").append(Component.literal("x" + ModUtils.formattedNumber(Math.pow(0.95, tier - 1))).withColor(hsvToArgb((Minecraft.getInstance().level.getGameTime() * 0.01f) % 1, 1, 1, 255)));
    }
}
