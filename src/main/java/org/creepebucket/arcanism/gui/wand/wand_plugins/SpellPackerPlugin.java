package org.creepebucket.arcanism.gui.wand.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.creepebucket.arcanism.gui.lib.api.Color;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.lib.api.SmoothedValue;
import org.creepebucket.arcanism.gui.lib.api.Widget;
import org.creepebucket.arcanism.gui.lib.widgets.*;
import org.creepebucket.arcanism.gui.wand.WandScreen;
import org.creepebucket.arcanism.gui.wand.WandWidgets;
import org.creepebucket.arcanism.registries.ModDataComponents;


import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;

import static net.minecraft.core.component.DataComponents.CUSTOM_NAME;
import static net.minecraft.util.Mth.hsvToArgb;
import static org.creepebucket.arcanism.Arcanism.MODID;

public class SpellPackerPlugin extends BasePlugin {
    public Widget bar;

    public SpellPackerPlugin() {
        pluginName = "spell_packer";
    }

    @Override
    public void onAdd(WandScreen screen) {
        bar = new Widget.BlankWidget(fromTopRight(0, 0), fromTopLeft(80, 0));
        screen.addTopbar(bar);

        var packedSpellDy = new SmoothedValue(-113);

        bar.addChild(new ImageButtonWidget(fromTopLeft(64, 0), fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_packed_spell.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_packed_spell.png"),
                () -> {
                    if (!screen.textureInputbox.box.getValue().matches("[0-9a-z_./]+")) {
                        screen.notificationWidget.addError(Component.translatable("gui.arcanism.wand.errors.invalid_input"));
                        return;
                    }
                    screen.getMenu().packSpellHook.trigger(screen.nameInputbox.box.getValue(), screen.descInputbox.box.getValue(), screen.textureInputbox.box.getValue());
                    screen.notificationWidget.addDebug(Component.translatable("gui.arcanism.wand.errors.export_successful"));
                }).tooltip(Component.translatable("gui.arcanism.wand.inventory.export_to_packed_spell")).dy(packedSpellDy).color(screen.mainColor));

        bar.addChild(new ImageButtonWidget(fromTopLeft(48, 0), fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_wand.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_wand.png"),
                () -> {
                    if (screen.getMenu().packedSpellContainer.isEmpty()) {
                        screen.notificationWidget.addError(Component.translatable("gui.arcanism.wand.errors.input_slot_empty"));
                        return;
                    }
                    for (WandWidgets.SpellStorageWidget widget : screen.storageSlots) {
                        widget.deleteHook = screen.getMenu().packedToStorageHook;
                        widget.delta2X.speed = Minecraft.getInstance().getWindow().getGuiScaledWidth() * 1.2;
                    }
                    screen.notificationWidget.addDebug(Component.translatable("gui.arcanism.wand.errors.export_successful"));
                }).tooltip(Component.translatable("gui.arcanism.wand.inventory.export_to_wand")).dy(packedSpellDy).color(screen.mainColor));

        bar.addChild(new ImageButtonWidget(fromTopLeft(16, 0), fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import_from_wand.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import_from_wand.png"),
                () -> {
                    if (screen.getMenu().packedSpellContainer.isEmpty()) {
                        screen.notificationWidget.addError(Component.translatable("gui.arcanism.wand.errors.input_slot_empty"));
                        return;
                    }
                    screen.getMenu().packSpellHook.trigger(screen.getMenu().packedSpellContainer.getItem(0).get(CUSTOM_NAME).getString(), screen.getMenu().packedSpellContainer.getItem(0).get(ModDataComponents.DESCRIPTION), screen.getMenu().packedSpellContainer.getItem(0).get(ModDataComponents.RESOURCE_LOCATION));
                    screen.notificationWidget.addDebug(Component.translatable("gui.arcanism.wand.errors.packing_successful"));
                }).tooltip(Component.translatable("gui.arcanism.wand.inventory.import_from_wand")).dy(packedSpellDy).color(screen.mainColor));

        bar.addChild(new ImageButtonWidget(fromTopLeft(0, 0), fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/add_to_supply.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/add_to_supply.png"),
                () -> {
                    if (!screen.textureInputbox.box.getValue().matches("[0-9a-z_./]+")) {
                        screen.notificationWidget.addError(Component.translatable("gui.arcanism.wand.errors.invalid_input"));
                        return;
                    }
                    screen.getMenu().packAndSupplyHook.trigger(screen.nameInputbox.box.getValue(), screen.descInputbox.box.getValue(), screen.textureInputbox.box.getValue());
                    screen.notificationWidget.addDebug(Component.translatable("gui.arcanism.wand.errors.packing_successful"));
                }).tooltip(Component.translatable("gui.arcanism.wand.inventory.add_to_supply")).dy(packedSpellDy).color(screen.mainColor));

        bar.addChild(new SlotWidget(screen.getMenu().packedSpellSlots.get(0), fromTopLeft(32, 0)).dy(packedSpellDy));
        bar.addChild(new RectangleWidget(fromTopLeft(33, 1), fromTopLeft(14, 14)).color(screen.bgColor).dy(packedSpellDy));

        screen.nameInputbox = ((InputBoxWidget) new InputBoxWidget(fromTopLeft(0, 32), fromTopLeft(80, 16),
                Component.translatable("gui.arcanism.wand.inventory.packed_spell_name_example").getString(), 1024).dy(packedSpellDy).bgColor(screen.bgColor).mainColor(new Color(0)).textColor(screen.textColor)).extendWhenFocus(220, -220);
        screen.descInputbox = ((InputBoxWidget) new InputBoxWidget(fromTopLeft(0, 64), fromTopLeft(80, 16),
                Component.translatable("gui.arcanism.wand.inventory.packed_spell_desc_example").getString(), 1024).dy(packedSpellDy).bgColor(screen.bgColor).mainColor(new Color(0)).textColor(screen.textColor)).extendWhenFocus(220, -220);
        screen.textureInputbox = ((InputBoxWidget) new InputBoxWidget(fromTopLeft(0, 96), fromTopLeft(80, 16),
                Component.translatable("gui.arcanism.wand.inventory.packed_spell_texture_example").getString(), 1024).dy(packedSpellDy).bgColor(screen.bgColor).mainColor(new Color(0)).textColor(screen.textColor)).extendWhenFocus(220, -220);

        bar.addChild(screen.nameInputbox);
        bar.addChild(screen.descInputbox);
        bar.addChild(screen.textureInputbox);

        bar.addChild(new TextureWidget(fromTopLeft(0, 16), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/nametag.png"), fromTopLeft(16, 16)).dy(packedSpellDy).color(screen.mainColor));
        bar.addChild(new TextureWidget(fromTopLeft(0, 48), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/description.png"), fromTopLeft(16, 16)).dy(packedSpellDy).color(screen.mainColor));
        bar.addChild(new TextureWidget(fromTopLeft(0, 80), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/directory.png"), fromTopLeft(16, 16)).dy(packedSpellDy).color(screen.mainColor));

        bar.addChild(new TextWidget(fromTopLeft(16, 20), Component.translatable("gui.arcanism.wand.inventory.packed_spell_name")).color(screen.textColor).dy(packedSpellDy));
        bar.addChild(new TextWidget(fromTopLeft(16, 52), Component.translatable("gui.arcanism.wand.inventory.packed_spell_desc")).color(screen.textColor).dy(packedSpellDy));
        bar.addChild(new TextWidget(fromTopLeft(16, 84), Component.translatable("gui.arcanism.wand.inventory.packed_spell_dir")).color(screen.textColor).dy(packedSpellDy));

        bar.addChild(new RectangleButtonWidget(fromTopLeft(0, 114), fromTopLeft(80, 5), () -> {
            if (packedSpellDy.get() != 7) packedSpellDy.set(7);
            else packedSpellDy.set(-113);
        }).mainColor(new Color(screen.mainColor.toArgbWithAlphaMult(0.5))).bgColor(screen.bgColor).dy(packedSpellDy));
    }

    @Override
    public void onRemove(WandScreen screen) {
        screen.removeTopbar(bar);
    }

    @Override
    public Component function() {
        return Component.translatable("gui.arcanism.wand.plugin.provide_ability")
                .append(Component.translatable("gui.arcanism.wand.plugin.spell_packing")
                        .withColor(hsvToArgb((Minecraft.getInstance().level.getGameTime() * 0.01f) % 1, 1, 1, 255)));
    }
}
