package org.creepebucket.programmable_magic.gui.wand.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
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
    public Widget background, lockCustomSupply, scroll, scrollBar, bottomUpperBound;
    public List<Widget> scrollWidgets, subCategoryJumps = new ArrayList<>();

    public SpellSupplyPlugin(int tier) {
        this.tier = tier;
        pluginName = "spell_supply_t" + tier;
    }

    @Override
    public void onAdd(WandScreen screen) {

        // 背景
        background = screen.addWidget(new RectangleWidget(Coordinate.fromTopLeft(7, 0), Coordinate.fromBottomLeft(80, 0)).color(screen.bgColor));

        var slotIndex = screen.getMenu().supplySlotsStartIndex;
        var supplyDy = new SmoothedValue(0);

        // 添加法术供应槽位
        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;

        var dx = 0;
        var dy = 0;
        var categoriesCount = 0;

        scrollWidgets = new ArrayList<>();

        // 可以滚动的部分
        for (String key : spells.keySet()) {
            var subCategorySpells = spells.get(key);
            dx = 0;
            int categoryDy = dy;

            // 用于快速跳转到该类别的按钮
            int finalCategoriesCount = categoriesCount;
            subCategoryJumps.add(screen.addWidget(new WandWidgets.WandSubcategoryJumpButton(new Coordinate((w, h) -> 0, (w, h) -> (finalCategoriesCount * h / (spells.size() + 1))),
                    new Coordinate((w, h) -> 7, (w, h) -> (((finalCategoriesCount + 1) * h / (spells.size() + 1)) - (finalCategoriesCount * h / (spells.size() + 1)))), supplyDy, -categoryDy + 20)
                    .mainColor(new Color(ModUtils.SPELL_COLORS().getOrDefault(key, 0xFFFFFFFF)))
                    .bgColor(new Color(new Color(ModUtils.SPELL_COLORS().getOrDefault(key, 0xFFFFFFFF)).toArgbWithAlphaMult(0.6)))
                    .tooltip(Component.translatable(key))));

            // 子类别标题
            scrollWidgets.add(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(dx + 8, categoryDy), key).dy(supplyDy));

            // 法术
            for (int i = 0; i < subCategorySpells.size(); i++) {
                var pos = Coordinate.fromTopLeft(dx % 80 + 8, categoryDy + Math.floorDiv(dx, 80) * 16 + 32);

                scrollWidgets.add(new SlotWidget(screen.getMenu().slots.get(slotIndex), pos).dy(supplyDy));
                slotIndex++;

                dx += 16;
            }
            dy = categoryDy + 64 + Math.floorDiv(dx - 16, 80) * 16; // 奇技淫巧和魔法数字的集大成者

            categoriesCount++;
        }

        dx = 0;
        // 自定义法术供应栏
        int customCategoryDy = dy;

        scrollWidgets.add(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(8, customCategoryDy), "spell." + MODID + ".subcategory.custom").dy(supplyDy));

        subCategoryJumps.add(screen.addWidget(new WandWidgets.RectangleButtonWidget(
                new Coordinate((w, h) -> 0, (w, h) -> (h - 1 - h / (spells.size() + 1))),
                new Coordinate((w, h) -> 7, (w, h) -> 1 + h / (spells.size() + 1)),
                () -> supplyDy.set(-customCategoryDy + 20))
                .mainColor(new Color(0xFF000000)).bgColor(new Color(0xB0000000)).tooltip(Component.translatable("spell." + MODID + ".subcategory.custom"))));

        for (WandSlots.CustomSupplySlot slot : screen.getMenu().customSupplySlots) {
            scrollWidgets.add(new SlotWidget(slot, Coordinate.fromTopLeft(dx + 8, customCategoryDy + 32 + Math.floorDiv(dx, 80) * 16)).dy(supplyDy));
            dx = (dx + 16) % 80;
        }

        int finalDy = customCategoryDy + 160 + 64;

        // 自定义供应的锁定按钮
        screen.lockButton = (SelectableImageButtonWidget) new SelectableImageButtonWidget(Coordinate.fromTopLeft(7, finalDy - 28), Coordinate.fromTopLeft(80, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/wand_lock_button.png"))
                .selectedTexture(Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/wand_unlock_button.png")).tooltip(Component.translatable("gui.programmable_magic.wand.spells.unlock_custom")).dy(supplyDy);
        lockCustomSupply = screen.lockButton;
        scrollWidgets.add(screen.lockButton);

        for (Widget widget : scrollWidgets) screen.addWidget(widget);

        // 滚动交互
        scroll = screen.addWidget(new ScrollRegionWidget(Coordinate.fromTopLeft(8, 0), Coordinate.fromTopLeft(80, 999),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), 16, supplyDy));
        // 滚动条
        scrollBar = screen.addWidget(new ScrollbarWidget(Coordinate.fromTopLeft(88, 0), Coordinate.fromBottomLeft(4, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), supplyDy, "y").reverseDirection());

        bottomUpperBound = screen.addWidget(new RectangleWidget(Coordinate.fromTopLeft(93, 0), Coordinate.fromBottomLeft(2, 0)).color(screen.mainColor));

        bottomUpperBound.addAnimation(new Animation.FadeIn.FromTop(.3), 0);
        background.addAnimation(new Animation.FadeIn.FromTop(.5), 0);
        lockCustomSupply.addAnimation(new Animation.FadeIn.FromTop(.3), 0);
        scrollBar.addAnimation(new Animation.FadeIn.FromTop(.5), .3);

        double t = 0;
        for (Widget widget : scrollWidgets.reversed()) {
            if (widget.y() > Minecraft.getInstance().getWindow().getGuiScaledHeight()) continue;
            widget.addAnimation(new Animation.FadeIn.FromLeft(.5), t);
            t += 0.01;
        }

        t = 0;
        for (Widget widget : subCategoryJumps) {
            widget.addAnimation(new Animation.FadeIn.FromLeft(.3), t);
            t += 0.03;
        }
    }

    @Override
    public void onRemove(WandScreen screen) {
        bottomUpperBound.addAnimation(new Animation.FadeOut.ToTop(.3), 0);
        background.addAnimation(new Animation.FadeOut.ToTop(.5), 0);
        lockCustomSupply.addAnimation(new Animation.FadeOut.ToTop(.3), 0);
        scrollBar.addAnimation(new Animation.FadeOut.ToTop(.5), .3);
        scroll.removeMyself();

        double t = 0;
        for (Widget widget : scrollWidgets.reversed()) {
            if (widget.y() > Minecraft.getInstance().getWindow().getGuiScaledHeight()) continue;
            widget.addAnimation(new Animation.FadeOut.ToLeft(.5), t);
            t += 0.01;
        }

        t = 0;
        for (Widget widget : subCategoryJumps) {
            widget.addAnimation(new Animation.FadeOut.ToLeft(.3), t);
            t += 0.03;
        }
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
        return Component.translatable("gui.programmable_magic.wand.plugin.mana_mult_mult")
                .append(Component.literal("x" + ModUtils.formattedNumber(Math.pow(0.95, tier - 1)))
                        .withColor(hsvToArgb((Minecraft.getInstance().level.getGameTime() * 0.01f) % 1, 1, 1, 255)));
    }
}
