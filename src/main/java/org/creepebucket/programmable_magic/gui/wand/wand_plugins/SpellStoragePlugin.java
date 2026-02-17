package org.creepebucket.programmable_magic.gui.wand.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollRegionWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollbarWidget;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.gui.wand.WandWidgets;

import java.util.List;

import static net.minecraft.util.Mth.hsvToArgb;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellStoragePlugin extends BasePlugin {
    public int tier;
    public Widget leftShift, rightShift, scroll, scrollBar, invTopBound, invRightBound;
    public List<WandWidgets.SpellStorageWidget> storageSlots;

    public SpellStoragePlugin(int tier) {
        this.tier = tier;
        pluginName = "spell_storage_t" + tier;
    }

    @Override
    public void onAdd(WandScreen screen) {
        // 法术存储

        var storageDx = new SmoothedValue(0);

        var spellCountCanFit = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 16 - 8;

        for (int i = 0; i < spellCountCanFit; i++) {
            int finalI = i;
            var storage = (WandWidgets.SpellStorageWidget) new WandWidgets.SpellStorageWidget(screen.getMenu().spellStoreSlots,
                    new Coordinate((w, h) -> (w - spellCountCanFit * 16) / 2 + 64 + finalI * 16, (w, h) -> h - 115),
                    i, screen.getMenu().storedSpellsEditHook, screen.getMenu().clearSpellsHook, screen.storageSlots).dx(storageDx).mainColor(screen.mainColor).textColor(screen.textColor).bgColor(screen.bgColor);
            screen.addWidget(storage);
            screen.storageSlots.add(storage);
        }

        storageSlots = screen.storageSlots;

        var targetMin = (-tier * 250 - 2) * 16 + spellCountCanFit * 16;

        // 两边遮挡
        leftShift = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomLeft(94, -115), Coordinate.fromTopLeft(32, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_left.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_left.png"),
                () -> storageDx.set(Math.clamp(storageDx.get() + 80, targetMin, 0)))
                .tooltip(Component.translatable("gui.programmable_magic.wand.spells.left_shift")).color(screen.mainColor));
        rightShift = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-30, -115), Coordinate.fromTopLeft(32, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_right.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_right.png"),
                () -> storageDx.set(Math.clamp(storageDx.get() - 80, targetMin, 0)))
                .tooltip(Component.translatable("gui.programmable_magic.wand.spells.right_shift")).color(screen.mainColor));

        // 滚动条
        scrollBar = screen.addWidget(new ScrollbarWidget(Coordinate.fromBottomLeft(96, -112 + 15), Coordinate.fromTopRight(-96, 4),
                Coordinate.fromTopLeft(targetMin, 0), storageDx, "X").mainColor(screen.mainColor));

        // 滚轮区域
        scroll = screen.addWidget(new ScrollRegionWidget(Coordinate.fromBottomLeft(94, -113), Coordinate.fromTopLeft(999, 16), Coordinate.fromTopLeft(targetMin, 0), 80, storageDx));

        invTopBound = screen.addWidget(new RectangleWidget(Coordinate.fromBottomLeft(95, -76 - 16), Coordinate.fromTopRight(0, 2)).color(screen.mainColor));
        invRightBound = screen.addWidget(new RectangleWidget(Coordinate.fromBottomLeft(95 + 18 * 9 + 2, -76 - 16), Coordinate.fromTopLeft(2, 92)).color(screen.mainColor));

        double t = 0;

        for (WandWidgets.SpellStorageWidget widget : storageSlots) {
            t += 0.02;
            widget.addAnimation(new Animation.FadeIn.FromRight(0.3), t);
        }

        leftShift.addAnimation(new Animation.FadeIn.FromRight(0.3), 0);
        rightShift.addAnimation(new Animation.FadeIn.FromRight(0.3), t);

        scrollBar.addAnimation(new Animation.FadeIn.FromRight(0.5), .05);
        scroll.removeMyself();

        invTopBound.addAnimation(new Animation.FadeIn.FromTop(0.3), .05);
        invRightBound.addAnimation(new Animation.FadeIn.FromRight(0.3), .05);
    }

    @Override
    public void onRemove(WandScreen screen) {
        double t = 0;

        for (WandWidgets.SpellStorageWidget widget : storageSlots) {
            t += 0.02;
            widget.addAnimation(new Animation.FadeOut.ToRight(0.3), t);
        }

        leftShift.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
        rightShift.addAnimation(new Animation.FadeOut.ToRight(0.3), t);

        scrollBar.addAnimation(new Animation.FadeOut.ToRight(0.5), .05);
        scroll.removeMyself();

        invTopBound.addAnimation(new Animation.FadeOut.ToTop(0.3), .05);
        invRightBound.addAnimation(new Animation.FadeOut.ToRight(0.3), .05);
    }

    @Override
    public Component function() {
        return Component.translatable("gui.programmable_magic.wand.plugin.spell_storage")
                .append(Component.literal("" + tier * 250)
                        .withColor(hsvToArgb((Minecraft.getInstance().level.getGameTime() * 0.01f) % 1, 1, 1, 255)));
    }
}
