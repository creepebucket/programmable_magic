package org.creepebucket.arcanism.gui.wand.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.creepebucket.arcanism.gui.lib.api.Color;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.lib.api.SmoothedValue;
import org.creepebucket.arcanism.gui.lib.api.Widget;
import org.creepebucket.arcanism.gui.lib.widgets.RectangleButtonWidget;
import org.creepebucket.arcanism.gui.lib.widgets.TextButtonWidget;
import org.creepebucket.arcanism.gui.lib.widgets.TextWidget;
import org.creepebucket.arcanism.gui.lib.widgets.TextureWidget;
import org.creepebucket.arcanism.gui.wand.WandScreen;
import org.creepebucket.arcanism.gui.wand.WandWidgets;

import java.util.ArrayList;


import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;

import static net.minecraft.util.Mth.hsvToArgb;
import static org.creepebucket.arcanism.Arcanism.MODID;

public class ColorThemePlugin extends BasePlugin {
    public Widget bar;
    public WandWidgets.ColorSelectionWidget main, bg, text;

    @Override
    public Component function() {
        return Component.translatable("gui.arcanism.wand.plugin.provide_ability")
                .append(Component.translatable("gui.arcanism.wand.plugin.change_theme")
                        .withColor(hsvToArgb((Minecraft.getInstance().level.getGameTime() * 0.01f) % 1, 1, 1, 255)));
    }

    @Override
    public void onAdd(WandScreen screen) {
        bar = new Widget.BlankWidget(fromTopRight(0, 0), fromTopLeft(100, 0));
        screen.addTopbar(bar);
        var dy = new SmoothedValue(-126);

        bar.addChild(new TextureWidget(ZERO, Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/adjust_spell.png"), fromTopLeft(16, 16))
                .dy(dy).color(screen.mainColor));
        bar.addChild(new TextWidget(fromTopLeft(16, 3), Component.translatable("gui.arcanism.wand.theme.title")).color(screen.textColor).dy(dy));

        bar.addChild(new TextWidget(fromTopLeft(0, 20), Component.translatable("gui.arcanism.wand.theme.main")).color(screen.textColor).dy(dy));
        main = (WandWidgets.ColorSelectionWidget) bar.addChild(new WandWidgets.ColorSelectionWidget(fromTopLeft(0, 30), screen.mainColor).dy(dy).color(screen.mainColor));

        bar.addChild(new TextWidget(fromTopLeft(0, 49), Component.translatable("gui.arcanism.wand.theme.background")).color(screen.textColor).dy(dy));
        bg = (WandWidgets.ColorSelectionWidget) bar.addChild(new WandWidgets.ColorSelectionWidget(fromTopLeft(0, 59), screen.bgColor).dy(dy).color(screen.mainColor));

        bar.addChild(new TextWidget(fromTopLeft(0, 78), Component.translatable("gui.arcanism.wand.theme.text")).color(screen.textColor).dy(dy));
        text = (WandWidgets.ColorSelectionWidget) bar.addChild(new WandWidgets.ColorSelectionWidget(fromTopLeft(0, 88), screen.textColor).dy(dy).color(screen.mainColor));

        bar.addChild(new TextButtonWidget(fromTopLeft(0, 110), fromTopLeft(100, 16), Component.translatable("gui.arcanism.wand.theme.reload"),
                () -> {
                    screen.mainColor = main.color();
                    screen.bgColor = new Color(bg.color().toArgbWithAlphaMult(0.5));
                    screen.textColor = text.color();

                    screen.root.children = new ArrayList<>();
                    screen.init();
                }).dy(dy).mainColor(screen.mainColor).bgColor(screen.bgColor).textColor(screen.textColor));

        bar.addChild(new RectangleButtonWidget(fromTopLeft(0, 127), fromTopLeft(100, 5), () -> {
            if (dy.get() != 7) dy.set(7);
            else dy.set(-126);
        }).mainColor(new Color(screen.mainColor.toArgbWithAlphaMult(0.5))).bgColor(screen.bgColor).dy(dy));
    }

    @Override
    public void onRemove(WandScreen screen) {
        screen.removeTopbar(bar);
    }
}
