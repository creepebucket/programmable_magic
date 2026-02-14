package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.core.component.DataComponents.CUSTOM_NAME;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;
import static org.creepebucket.programmable_magic.registries.WandPluginRegistry.getPlugin;

public class WandScreen extends Screen<WandMenu> {

    public static double dt;
    public double spellSupplyDeltaYSpeed = 0;
    public double packedSpellDeltaYSpeed = 0;
    public double pluginDeltaYSpeed = 0;
    public double spellSupplyAccurateDeltaY = this.menu.supplySlotDeltaY.get();
    public double packedSpellAccurateDeltaY = this.menu.packedSpellDeltaY.get();
    public double pluginAccurateDeltaY = this.menu.pluginDeltaY.get();
    public List<WandWidgets.SpellStorageWidget> storageSlots = new ArrayList<>();
    public Double lastFrame = System.nanoTime() / 1e9;
    public SelectableImageButtonWidget bypassCompileWidget;
    public InputBoxWidget nameInputbox, descInputbox, textureInputbox;
    public WandWidgets.WandNotificationWidget notificationWidget;
    public WandWidgets.DySelectableImageButtonWidget lockButton;

    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        for (ItemStack plugin : menu.pluginContainer) {
            if (plugin.isEmpty()) continue;
            getPlugin(plugin.getItem()).onAdd(this);
        }

        /* ===========法术供应段=========== */
        var packedSpellDeltaY = this.menu.packedSpellDeltaY;
        var packedSpellTargetDeltaY = this.menu.packedSpellTargetDeltaY;
        var pluginDeltaY = this.menu.pluginDeltaY;
        var pluginTargetDeltaY = this.menu.pluginTargetDeltaY;
        /* ===========法术储存段=========== */

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
                    notificationWidget.addDebug(Component.literal("im a debug message"));
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_step")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 2), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_tick.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_tick.png"),
                () -> {
                    notificationWidget.addInfo(Component.literal("im an info"));
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_tick")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 3), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_resume.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_resume.png"),
                () -> {
                    notificationWidget.addWarning(Component.literal("im a warning"));
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_resume")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-16, -68 - 14 + 16 * 4), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_pause.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/debugger_pause.png"),
                () -> {
                    notificationWidget.addError(Component.literal("im an error hehe"));
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
                    for (WandWidgets.SpellStorageWidget widget : storageSlots) {
                        widget.deleteHook = menu.clearSpellsHook;
                        widget.acc2 = Minecraft.getInstance().getWindow().getGuiScaledWidth() * 1.2;
                    }
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_delete")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-80 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import.png"),
                () -> {
                    for (WandWidgets.SpellStorageWidget widget : storageSlots) {
                        widget.deleteHook = menu.importSpellsHook;
                        widget.acc2 = Minecraft.getInstance().getWindow().getGuiScaledWidth() * 1.2;
                    }
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_import")));
        addWidget(new ImageButtonWidget(Coordinate.fromBottomRight(-96 - 2, -76 - 14), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/left_shift.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/left_shift.png"),
                () -> {
                    menu.storedSpellsEditHook.trigger(-1, true);
                    for (WandWidgets.SpellStorageWidget widget : storageSlots) widget.delta2X += 16;
                }, Component.translatable("gui.programmable_magic.wand.inventory.debugger_left_shift")));

        // 编译相关
        bypassCompileWidget = new SelectableImageButtonWidget(Coordinate.fromBottomLeft(261, -90), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/compile.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/bypass_compile.png"),
                Component.translatable("gui.programmable_magic.wand.inventory.debugger_bypass_compile"));
        addWidget(bypassCompileWidget);

        addWidget(new TextureWidget(Coordinate.fromBottomLeft(261, -72), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/errors.png"), Coordinate.fromTopLeft(16, 16)));
        addWidget(new TextWidget(Coordinate.fromBottomLeft(261 + 16, -68), Component.translatable("gui.programmable_magic.wand.inventory.debugger_compile_errors"), -1));
        addWidget(new WandWidgets.CompileErrorWidget(Coordinate.fromBottomLeft(261 + 16, -68 + 16)));

        // 发射按钮
        addWidget(new WandWidgets.SpellReleaseWidget(Coordinate.fromBottomLeft(261 + 16, -88), Coordinate.fromTopRight(-261 - 16 - 98, 12)));

        /* ===========法术包装器=========== */

        addWidget(new WandWidgets.DyImageButtonWidget(Coordinate.fromTopRight(-16 - 2, 0), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_packed_spell.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_packed_spell.png"),
                () -> {
                    if (!textureInputbox.box.getValue().matches("[0-9a-z_./]+")) {
                        notificationWidget.addError(Component.translatable("gui.programmable_magic.wand.errors.invalid_input"));
                        return;
                    }
                    menu.packSpellHook.trigger(nameInputbox.box.getValue(), descInputbox.box.getValue(), textureInputbox.box.getValue());
                    notificationWidget.addDebug(Component.translatable("gui.programmable_magic.wand.errors.export_successful"));
                }, Component.translatable("gui.programmable_magic.wand.inventory.export_to_packed_spell"), packedSpellDeltaY));
        addWidget(new WandWidgets.DyImageButtonWidget(Coordinate.fromTopRight(-32 - 2, 0), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_wand.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/export_to_wand.png"),
                () -> {
                    if (menu.packedSpellContainer.isEmpty()) {
                        notificationWidget.addError(Component.translatable("gui.programmable_magic.wand.errors.input_slot_empty"));
                        return;
                    }
                    for (WandWidgets.SpellStorageWidget widget : storageSlots) {
                        widget.deleteHook = menu.packedToStorageHook;
                        widget.acc2 = Minecraft.getInstance().getWindow().getGuiScaledWidth() * 1.2;
                    }
                    notificationWidget.addDebug(Component.translatable("gui.programmable_magic.wand.errors.export_successful"));
                }, Component.translatable("gui.programmable_magic.wand.inventory.export_to_wand"), packedSpellDeltaY));
        addWidget(new WandWidgets.DyImageButtonWidget(Coordinate.fromTopRight(-64 - 2, 0), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import_from_wand.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/import_from_wand.png"),
                () -> {
                    if (menu.packedSpellContainer.isEmpty()) {
                        notificationWidget.addError(Component.translatable("gui.programmable_magic.wand.errors.input_slot_empty"));
                        return;
                    }
                    menu.packSpellHook.trigger(menu.packedSpellContainer.getItem(0).get(CUSTOM_NAME).getString(), menu.packedSpellContainer.getItem(0).get(ModDataComponents.DESCRIPTION), menu.packedSpellContainer.getItem(0).get(ModDataComponents.RESOURCE_LOCATION));
                    notificationWidget.addDebug(Component.translatable("gui.programmable_magic.wand.errors.packing_successful"));

                }, Component.translatable("gui.programmable_magic.wand.inventory.import_from_wand"), packedSpellDeltaY));
        addWidget(new WandWidgets.DyImageButtonWidget(Coordinate.fromTopRight(-80 - 2, 0), Coordinate.fromTopLeft(16, 16),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/add_to_supply.png"), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/add_to_supply.png"),
                () -> {
                    if (!textureInputbox.box.getValue().matches("[0-9a-z_./]+")) {
                        notificationWidget.addError(Component.translatable("gui.programmable_magic.wand.errors.invalid_input"));
                        return;
                    }
                    menu.packAndSupplyHook.trigger(nameInputbox.box.getValue(), descInputbox.box.getValue(), textureInputbox.box.getValue());
                    notificationWidget.addDebug(Component.translatable("gui.programmable_magic.wand.errors.packing_successful"));
                }, Component.translatable("gui.programmable_magic.wand.inventory.add_to_supply"), packedSpellDeltaY));

        addWidget(new WandWidgets.DySlotWidget(menu.packedSpellSlots.get(0), Coordinate.fromTopRight(-48 - 2, 0), packedSpellDeltaY));
        addWidget(new WandWidgets.DyRectangleWidget(Coordinate.fromTopRight(-47 - 2, 1), Coordinate.fromTopLeft(14, 14), -2147483648, packedSpellDeltaY));

        nameInputbox = new WandWidgets.DxDyInputBoxWidget(Coordinate.fromTopRight(-80 - 2, 32), Coordinate.fromTopLeft(80, 16),
                Component.translatable("gui.programmable_magic.wand.inventory.packed_spell_name_example").getString(), 1024, -1, 0, -2147483647, packedSpellDeltaY);
        descInputbox = new WandWidgets.DxDyInputBoxWidget(Coordinate.fromTopRight(-80 - 2, 64), Coordinate.fromTopLeft(80, 16),
                Component.translatable("gui.programmable_magic.wand.inventory.packed_spell_desc_example").getString(), 1024, -1, 0, -2147483647, packedSpellDeltaY);
        textureInputbox = new WandWidgets.DxDyInputBoxWidget(Coordinate.fromTopRight(-80 - 2, 96), Coordinate.fromTopLeft(80, 16),
                Component.translatable("gui.programmable_magic.wand.inventory.packed_spell_texture_example").getString(), 1024, -1, 0, -2147483647, packedSpellDeltaY);

        addWidget(nameInputbox);
        addWidget(descInputbox);
        addWidget(textureInputbox);

        addWidget(new WandWidgets.DyTextureWidget(Coordinate.fromTopRight(-80 - 2, 16), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/nametag.png"), Coordinate.fromTopLeft(16, 16), packedSpellDeltaY));
        addWidget(new WandWidgets.DyTextureWidget(Coordinate.fromTopRight(-80 - 2, 48), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/description.png"), Coordinate.fromTopLeft(16, 16), packedSpellDeltaY));
        addWidget(new WandWidgets.DyTextureWidget(Coordinate.fromTopRight(-80 - 2, 80), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/directory.png"), Coordinate.fromTopLeft(16, 16), packedSpellDeltaY));

        addWidget(new WandWidgets.DyTextWidget(Coordinate.fromTopRight(-64 - 2, 20), Component.translatable("gui.programmable_magic.wand.inventory.packed_spell_name"), -1, packedSpellDeltaY));
        addWidget(new WandWidgets.DyTextWidget(Coordinate.fromTopRight(-64 - 2, 52), Component.translatable("gui.programmable_magic.wand.inventory.packed_spell_desc"), -1, packedSpellDeltaY));
        addWidget(new WandWidgets.DyTextWidget(Coordinate.fromTopRight(-64 - 2, 84), Component.translatable("gui.programmable_magic.wand.inventory.packed_spell_dir"), -1, packedSpellDeltaY));

        addWidget(new WandWidgets.DyRectangleButtonWidget(Coordinate.fromTopRight(-80 - 2, 114), Coordinate.fromTopLeft(80, 5),
                -2147483648, 0x80FFFFFF, packedSpellDeltaY, () -> {
            if (packedSpellTargetDeltaY.get() != 7) packedSpellTargetDeltaY.set(7);
            else packedSpellTargetDeltaY.set(-114);
        }));

        /* ===========插件=========== */

        // 标题
        addWidget(new WandWidgets.DyTextureWidget(Coordinate.fromTopRight(-164, 0), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/wand_plugins.png"), Coordinate.fromTopLeft(16, 16), pluginDeltaY));
        addWidget(new WandWidgets.DyTextWidget(Coordinate.fromTopRight(-164 + 16, 3), Component.translatable("gui.programmable_magic.wand.inventory.plugins"), -1, pluginDeltaY));

        // 插件
        for (int i = 0; i < menu.pluginContainer.getContainerSize(); i++)
            addWidget(new WandWidgets.PluginWidget(menu.pluginSlots.get(i), Coordinate.fromTopRight(-164, 20 + 20 * i), pluginDeltaY, -1, -2147483648));

        // 按钮
        addWidget(new WandWidgets.DyRectangleButtonWidget(Coordinate.fromTopRight(-164, 20 + 20 * menu.pluginContainer.getContainerSize()), Coordinate.fromTopLeft(80, 5),
                -2147483648, 0x80FFFFFF, pluginDeltaY, () -> {
            if (pluginTargetDeltaY.get() != 7) pluginTargetDeltaY.set(7);
            else pluginTargetDeltaY.set(-(20 + 20 * menu.pluginContainer.getContainerSize()));
        }));

        /* ===========边界装饰=========== */

        // 法术储存段

        // 玩家物品栏
        addWidget(new RectangleWidget(Coordinate.fromBottomLeft(95, -76 - 16), Coordinate.fromTopRight(0, 2), -1));

        addWidget(new TextureWidget(Coordinate.fromBottomLeft(95, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/backpack.png"), Coordinate.fromTopLeft(16, 16)));
        addWidget(new TextureWidget(Coordinate.fromBottomLeft(98 + 9 * 18 - 49, -76 - 14), Identifier.fromNamespaceAndPath(MODID, "textures/gui/ui/slant_end_bar_up.png"), Coordinate.fromTopLeft(48, 16)));
        addWidget(new TextWidget(Coordinate.fromBottomLeft(95 + 16, -76 - 10), Component.translatable("gui.programmable_magic.wand.inventory"), -1));

        addWidget(new RectangleWidget(Coordinate.fromBottomLeft(95 + 18 * 9 + 2, -76 - 16), Coordinate.fromTopLeft(2, 92), -1));

        // 法术调试器
        addWidget(new RectangleWidget(Coordinate.fromBottomRight(-18, -92), Coordinate.fromTopLeft(2, 92), -1));
        addWidget(new RectangleWidget(Coordinate.fromBottomRight(-9, -92 + 16 + 5), Coordinate.fromTopLeft(2, 2), -1));
        addWidget(new RectangleWidget(Coordinate.fromBottomLeft(261, -90 + 16), Coordinate.fromTopRight(-261 - 18, 2), -1));

        /* ===========其他=========== */

        notificationWidget = new WandWidgets.WandNotificationWidget(Coordinate.fromTopLeft(100, 0), Coordinate.fromTopLeft(128, 0));
        addWidget(notificationWidget);

        addWidget(new MouseCursorWidget());
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
        spellSupplyAccurateDeltaY = newDy;
        menu.supplySlotDeltaY.set((int) newDy);

        // 平滑 packedSpellDeltaY

        current = packedSpellAccurateDeltaY;
        target = (double) menu.packedSpellTargetDeltaY.get();

        // 核心科技, 从chatgpt偷的
        packedSpellDeltaYSpeed += (target - packedSpellAccurateDeltaY) * MaGiCaL_CoNsTaNt_1 * dt - packedSpellDeltaYSpeed * MaGiCaL_CoNsTaNt_2 * dt;

        newDy = current + packedSpellDeltaYSpeed * dt;
        packedSpellAccurateDeltaY = newDy;
        menu.packedSpellDeltaY.set((int) newDy);

        // 平滑 pluginDeltaY

        current = pluginAccurateDeltaY;
        target = (double) menu.pluginTargetDeltaY.get();

        // 核心科技, 从chatgpt偷的
        pluginDeltaYSpeed += (target - pluginAccurateDeltaY) * MaGiCaL_CoNsTaNt_1 * dt - pluginDeltaYSpeed * MaGiCaL_CoNsTaNt_2 * dt;

        newDy = current + pluginDeltaYSpeed * dt;
        pluginAccurateDeltaY = newDy;
        menu.pluginDeltaY.set((int) newDy);
    }

    @Override
    public void resize(int width, int height) {
        // 动态地在大小改变时重建控件
        this.menu.widgets = new ArrayList<>();
        super.resize(width, height);
    }
}
