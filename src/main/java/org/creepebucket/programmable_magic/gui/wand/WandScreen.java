package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandScreen extends Screen<WandMenu> {

    public List<WandWidgets.SpellStorageWidget> storageSlots = new ArrayList<>();
    public SelectableImageButtonWidget bypassCompileWidget;
    public SelectableImageButtonWidget lockButton;
    public InputBoxWidget nameInputbox, descInputbox, textureInputbox;
    public WandWidgets.WandNotificationWidget notificationWidget;
    public Color mainColor, bgColor, textColor;
    public List<Widget> bars = new ArrayList<>();

    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        mainColor = new Color(menu.wand.getOrDefault(ModDataComponents.THEME_MAIN_COLOR.get(), 0xFFFFFFFF));
        bgColor = new Color(menu.wand.getOrDefault(ModDataComponents.THEME_BG_COLOR.get(), 0x80000000));
        textColor = new Color(menu.wand.getOrDefault(ModDataComponents.THEME_TEXT_COLOR.get(), 0xFFFFFFFF));
    }

    public void addTopbar(Widget bar) {
        addWidget(bar);
        bars.add(bar);

        // 重建bar dx
        double delta = 0;

        for (Widget w : bars.reversed()) {
            delta -= w.w() + 2;
            w.dx.set(delta);
        }
    }

    public void removeTopbar(Widget bar) {
        bars.remove(bar);
        bar.addAnimation(new Animation.FadeOut.ToTop(0.3), 0);

        // 重建bar dx
        double delta = 0;

        for (Widget w : bars.reversed()) {
            delta -= w.w() + 2;
            w.dx.set(delta);
        }
    }

    @Override
    public void init() {
        super.init();
        // 玩家物品栏
        List<Slot> inventorySlots = menu.hotbarSlots;
        inventorySlots.addAll(menu.backpackSlots);

        for (int i = 0; i < 36; i++) {
            addWidget(new SlotWidget(inventorySlots.get(i), Coordinate.fromBottomLeft(97 + 18 * (i % 9), 18 * (i / 9) - 72)).addAnimation(new Animation.FadeIn.FromBottom(.3), 0.1 + 0.1 * Math.floorDiv(i, 9)));
            addWidget(new RectangleWidget(Coordinate.fromBottomLeft(97 + 18 * (i % 9), 18 * (i / 9) - 72), Coordinate.fromTopLeft(16, 16))
                    .color(i < 9 ? new Color(mainColor.toArgbWithAlphaMult(0.5)) : bgColor).addAnimation(new Animation.FadeIn.FromBottom(.3), 0.1 + 0.1 * Math.floorDiv(i, 9)));
        }

        addWidget(new TextureWidget(Coordinate.fromBottomLeft(95, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/backpack.png"),
                Coordinate.fromTopLeft(16, 16)).addAnimation(new Animation.FadeIn.FromBottom(.3), 0).color(mainColor));
        addWidget(new TextureWidget(Coordinate.fromBottomLeft(98 + 9 * 18 - 49, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/slant_end_bar_up.png"),
                Coordinate.fromTopLeft(48, 16)).addAnimation(new Animation.FadeIn.FromBottom(.3), 0).color(mainColor));
        addWidget(new TextWidget(Coordinate.fromBottomLeft(95 + 16, -76 - 10), Component.translatable("gui.programmable_magic.wand.inventory"))
                .color(textColor).addAnimation(new Animation.FadeIn.FromBottom(.3), 0));

        /* ===========插件=========== */

        var pluginDy = new SmoothedValue(-(19 + 20 * menu.pluginContainer.getContainerSize()));

        var bar = new Widget.BlankWidget(Coordinate.fromTopRight(0, 0), Coordinate.fromTopLeft(120, 0)).dy(pluginDy);
        addTopbar(bar);

        // 标题
        bar.addChild(new TextureWidget(Coordinate.fromTopLeft(0, 0), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/wand_plugins.png"), Coordinate.fromTopLeft(16, 16)).color(mainColor));
        bar.addChild(new TextWidget(Coordinate.fromTopLeft(16, 3), Component.translatable("gui.programmable_magic.wand.inventory.plugins")).color(textColor));

        // 插件
        for (int i = 0; i < menu.pluginContainer.getContainerSize(); i++)
            bar.addChild(new WandWidgets.PluginWidget(menu.pluginSlots.get(i), Coordinate.fromTopLeft(104, 20 + 20 * i)).textColor(textColor).bgColor(bgColor));

        // 按钮
        bar.addChild(new WandWidgets.RectangleButtonWidget(Coordinate.fromTopLeft(0, 20 + 20 * menu.pluginContainer.getContainerSize()), Coordinate.fromTopLeft(120, 5), () -> {
            if (pluginDy.get() != 7) pluginDy.set(7);
            else pluginDy.set(-(19 + 20 * menu.pluginContainer.getContainerSize()));
        }).mainColor(new Color(mainColor.toArgbWithAlphaMult(0.5))).bgColor(bgColor));

        /* ===========其他=========== */

        notificationWidget = new WandWidgets.WandNotificationWidget(Coordinate.fromTopLeft(100, 0), Coordinate.fromTopLeft(128, 0));
        addWidget(notificationWidget);

        // addWidget(new MouseCursorWidget());
    }

    @Override
    public void resize(int width, int height) {
        // 动态地在大小改变时重建控件
        root = new Widget.Root();
        super.resize(width, height);
    }

    @Override
    public void onClose() {
        menu.saveThemeHook.trigger(mainColor.toArgb(), bgColor.toArgb(), textColor.toArgb());
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        for (ItemStack plugin : menu.pluginContainer)
            if (WandPluginRegistry.isPlugin(plugin.getItem()))
                WandPluginRegistry.Client.getClientLogic(plugin.getItem()).render(this, graphics, mouseX, mouseY, partialTick);
    }
}
