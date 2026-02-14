package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollRegionWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollbarWidget;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.gui.wand.WandWidgets;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellStoragePlugin extends BasePlugin {
    public int tier;

    public SpellStoragePlugin(int tier) {
        this.tier = tier;
        pluginName = "spell_storage_t" + tier;
    }

    @Override
    public void onAdd(WandScreen screen) {
        var spellSlotTargetDeltaX = screen.getMenu().spellSlotTargetDeltaX;

        var spellCountCanFit = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 16 - 8;

        for (int i = 0; i < spellCountCanFit; i++) {
            var pos = new Coordinate((w, h) -> (w - spellCountCanFit * 16) / 2 + 64, (w, h) -> h - 115);

            var storage = new WandWidgets.SpellStorageWidget(screen.getMenu().spellStoreSlots, pos, i, screen.getMenu().storedSpellsEditHook, screen.getMenu().clearSpellsHook, screen.storageSlots, spellSlotTargetDeltaX);
            screen.addWidget(storage);
            screen.storageSlots.add(storage);
        }

        var targetMin = (-tier * 250 - 2) * 16 + spellCountCanFit * 16;

        // 两边遮挡
        screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomLeft(94, -115), Coordinate.fromTopLeft(32, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_left.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_left.png"),
                () -> {
                    spellSlotTargetDeltaX.set(Math.clamp(spellSlotTargetDeltaX.get() + 80, targetMin, 0));
                }, Component.translatable("gui.programmable_magic.wand.spells.left_shift")));
        screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-30, -115), Coordinate.fromTopLeft(32, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_right.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/stright_end_bar_right.png"),
                () -> {
                    spellSlotTargetDeltaX.set(Math.clamp(spellSlotTargetDeltaX.get() - 80, targetMin, 0));
                }, Component.translatable("gui.programmable_magic.wand.spells.right_shift")));

        // 滚动条
        screen.addWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromBottomLeft(96, -112 + 15), Coordinate.fromTopRight(-96, 4),
                Coordinate.fromTopLeft(targetMin, 0), spellSlotTargetDeltaX, -1, "X", false));

        // 滚轮区域
        screen.addWidget(new ScrollRegionWidget(Coordinate.fromBottomLeft(94, -113), Coordinate.fromTopLeft(999, 16), Coordinate.fromTopLeft(targetMin, 0), 80, spellSlotTargetDeltaX));
    }

    @Override
    public Component function() {
        return Component.translatable("gui.programmable_magic.wand.plugin.spell_storage" + tier * 250);
    }
}
