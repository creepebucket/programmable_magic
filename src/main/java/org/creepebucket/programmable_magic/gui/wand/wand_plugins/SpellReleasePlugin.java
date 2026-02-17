package org.creepebucket.programmable_magic.gui.wand.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.gui.wand.WandWidgets;

import static net.minecraft.util.Mth.hsvToArgb;
import static org.creepebucket.programmable_magic.ModUtils.formattedNumber;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellReleasePlugin extends BasePlugin {
    public int tier;
    public Widget debuggerIcon, debuggerStep, debuggerTick, debuggerResume, debuggerPause;
    public Widget editRightShift, editExport, editDelete, editImport, editLeftshift;
    public Widget bypassCompile, compileErrorIcon, compileErrorText, compileError;
    public Widget releaseButton, debuggerLeftBorder, debuggerSeperator, releaseButtonBottomBorder;

    public SpellReleasePlugin(int tier) {
        this.tier = tier;
        pluginName = "spell_release_t" + tier;
    }

    @Override
    public void onAdd(WandScreen screen) {

        debuggerIcon = screen.addWidget(new TextureWidget(Coordinate.fromBottomRight(-16, -76 - 14),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger.png"), Coordinate.fromTopLeft(16, 16))
                .addAnimation(new Animation.FadeIn.FromRight(0.3), 0).color(screen.mainColor));

        // 调试
        debuggerStep = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_step.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_step.png"),
                () -> {
                    screen.notificationWidget.addDebug(Component.literal("im a debug message"));
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_step")).addAnimation(new Animation.FadeIn.FromRight(0.3), .05).color(screen.mainColor));
        debuggerTick = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 2), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_tick.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_tick.png"),
                () -> {
                    screen.notificationWidget.addInfo(Component.literal("im an info"));
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_tick")).addAnimation(new Animation.FadeIn.FromRight(0.3), .05).color(screen.mainColor));
        debuggerResume = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 3), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_resume.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_resume.png"),
                () -> {
                    screen.notificationWidget.addWarning(Component.literal("im a warning"));
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_resume")).addAnimation(new Animation.FadeIn.FromRight(0.3), .05).color(screen.mainColor));
        debuggerPause = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 4), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_pause.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_pause.png"),
                () -> {
                    screen.notificationWidget.addError(Component.literal("im an error hehe"));
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_pause")).addAnimation(new Animation.FadeIn.FromRight(0.3), .05).color(screen.mainColor));

        // 编辑
        editRightShift = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-32 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/right_shift.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/right_shift.png"),
                () -> {
                    screen.getMenu().storedSpellsEditHook.trigger(-1, false);
                    for (WandWidgets.SpellStorageWidget widget : screen.storageSlots)
                        widget.addAnimation(new WandWidgets.SpellStorageWidget.MoveRight(), 0);
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_right_shift")).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0).color(screen.mainColor));
        editExport = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-48 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export.png"),
                () -> {
                    Minecraft.getInstance().keyboardHandler.setClipboard(ModUtils.serializeSpells(screen.getMenu().storedSpells));
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_export")).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0).color(screen.mainColor));
        editDelete = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-64 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/trashcan.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/trashcan.png"),
                () -> {
                    for (WandWidgets.SpellStorageWidget widget : screen.storageSlots) {
                        widget.deleteHook = screen.getMenu().clearSpellsHook;
                        widget.delta2X.set(Minecraft.getInstance().getWindow().getGuiScaledWidth());
                    }
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_delete")).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0).color(screen.mainColor));
        editImport = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-80 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import.png"),
                () -> {
                    for (WandWidgets.SpellStorageWidget widget : screen.storageSlots) {
                        widget.deleteHook = screen.getMenu().importSpellsHook;
                        widget.delta2X.set(Minecraft.getInstance().getWindow().getGuiScaledWidth());
                    }
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_import")).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0).color(screen.mainColor));
        editLeftshift = screen.addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-96 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/left_shift.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/left_shift.png"),
                () -> {
                    screen.getMenu().storedSpellsEditHook.trigger(-1, true);
                    for (WandWidgets.SpellStorageWidget widget : screen.storageSlots)
                        widget.addAnimation(new WandWidgets.SpellStorageWidget.MoveLeft(), 0);
                }).tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_left_shift")).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0).color(screen.mainColor));

        // 编译相关
        screen.bypassCompileWidget = (SelectableImageButtonWidget) new SelectableImageButtonWidget(Coordinate.fromBottomLeft(261, -90), Coordinate.fromTopLeft(16, 16), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/compile.png"))
                .selectedTexture(Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/bypass_compile.png"))
                .tooltip(Component.translatable("gui.programmable_magic.wand.inventory.debugger_bypass_compile")).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0).color(screen.mainColor).textColor(screen.textColor);
        bypassCompile = screen.addWidget(screen.bypassCompileWidget);

        compileErrorIcon = screen.addWidget(new TextureWidget(Coordinate.fromBottomLeft(261, -72), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/errors.png"),
                Coordinate.fromTopLeft(16, 16)).addAnimation(new Animation.FadeIn.FromBottom(0.3), .1).color(screen.mainColor));
        compileErrorText = screen.addWidget(new TextWidget(Coordinate.fromBottomLeft(261 + 16, -68),
                Component.translatable("gui.programmable_magic.wand.inventory.debugger_compile_errors")).addAnimation(new Animation.FadeIn.FromBottom(0.3), .1).color(screen.textColor));
        compileError = screen.addWidget(new WandWidgets.CompileErrorWidget(Coordinate.fromBottomLeft(261 + 16, -68 + 16)).addAnimation(new Animation.FadeIn.FromBottom(0.3), .1).mainColor(screen.mainColor).textColor(screen.textColor));

        // 发射按钮
        releaseButton = screen.addWidget(new WandWidgets.SpellReleaseWidget(Coordinate.fromBottomLeft(261 + 16, -88), Coordinate.fromTopRight(-261 - 16 - 98, 12)).addAnimation(new Animation.FadeIn.FromBottom(0.3), .05).color(screen.mainColor));

        debuggerLeftBorder = screen.addWidget(new RectangleWidget(Coordinate.fromBottomRight(-18, -92),
                Coordinate.fromTopLeft(2, 92)).color(screen.mainColor).addAnimation(new Animation.FadeIn.FromRight(0.3), .2));
        debuggerSeperator = screen.addWidget(new RectangleWidget(Coordinate.fromBottomRight(-9, -92 + 16 + 5),
                Coordinate.fromTopLeft(2, 2)).color(screen.mainColor).addAnimation(new Animation.FadeIn.FromRight(0.3), .15));
        releaseButtonBottomBorder = screen.addWidget(new RectangleWidget(Coordinate.fromBottomLeft(261, -90 + 16),
                Coordinate.fromTopRight(-261 - 18, 2)).color(screen.mainColor).addAnimation(new Animation.FadeIn.FromBottom(0.3), .15));
    }

    @Override
    public void onRemove(WandScreen screen) {
        debuggerIcon.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
        debuggerStep.addAnimation(new Animation.FadeOut.ToRight(0.3), .05);
        debuggerTick.addAnimation(new Animation.FadeOut.ToRight(0.3), .05);
        debuggerResume.addAnimation(new Animation.FadeOut.ToRight(0.3), .05);
        debuggerPause.addAnimation(new Animation.FadeOut.ToRight(0.3), .05);
        debuggerSeperator.addAnimation(new Animation.FadeOut.ToRight(0.3), .1);
        debuggerLeftBorder.addAnimation(new Animation.FadeOut.ToRight(0.3), .15);

        editRightShift.addAnimation(new Animation.FadeOut.ToBottom(0.3), .25);
        editDelete.addAnimation(new Animation.FadeOut.ToBottom(0.3), .25);
        editExport.addAnimation(new Animation.FadeOut.ToBottom(0.3), .25);
        editImport.addAnimation(new Animation.FadeOut.ToBottom(0.3), .25);
        editLeftshift.addAnimation(new Animation.FadeOut.ToBottom(0.3), .25);
        bypassCompile.addAnimation(new Animation.FadeOut.ToBottom(0.3), .25);

        releaseButton.addAnimation(new Animation.FadeOut.ToBottom(0.3), .15);
        compileError.addAnimation(new Animation.FadeOut.ToBottom(0.3), .05);
        compileErrorIcon.addAnimation(new Animation.FadeOut.ToBottom(0.3), .05);
        compileErrorText.addAnimation(new Animation.FadeOut.ToBottom(0.3), .05);

        releaseButtonBottomBorder.addAnimation(new Animation.FadeOut.ToBottom(0.3), 0);
    }

    @Override
    public void adjustWandValues(ModUtils.WandValues values, ItemStack pluginStack) {
        values.chargeRateW += Math.pow(8, tier - 1) * 1024;
    }

    @Override
    public Component function() {
        return Component.translatable("gui.programmable_magic.wand.plugin.charge_rate")
                .append(Component.literal(formattedNumber(Math.pow(8, tier - 1) * 1024)).append("W")
                        .withColor(hsvToArgb((Minecraft.getInstance().level.getGameTime() * 0.01f) % 1, 1, 1, 255)));
    }
}
