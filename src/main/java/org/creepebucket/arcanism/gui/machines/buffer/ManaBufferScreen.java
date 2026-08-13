package org.creepebucket.arcanism.gui.machines.buffer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.lib.widgets.*;
import org.creepebucket.arcanism.gui.machines.api.MachineScreen;
import org.creepebucket.arcanism.gui.machines.api.MachineWidgets;
import org.creepebucket.arcanism.utils.ModColors;

import java.util.List;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;
import static org.creepebucket.arcanism.gui.lib.api.ThemeTemplate.*;

public class ManaBufferScreen extends MachineScreen<ManaBufferMenu> {

	public ManaBufferScreen(ManaBufferMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_M);

		var totalCache = DynamicValue.fromSupplier(() -> menu.radiationCacheJ.get() + menu.temperatureCacheJ.get() + menu.momentumCacheJ.get() + menu.pressureCacheJ.get());
		var netPower = DynamicValue.fromSupplier(() -> menu.radiationPowerW.get() + menu.temperaturePowerW.get() + menu.momentumPowerW.get() + menu.pressurePowerW.get());

		// ================= 总览窗口 ================= //

		var overview = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(0, -135), fromTopLeft(380, 80), Component.translatable("gui.arcanism.machine.buffer.window.overview"), 0, 0));
		overview.addChild(new GridentRectangleWidget(fromTopLeft(0, 11), fromTopRight(0, 2)).vertical().color(ModColors.MAIN_COLOR_M.withAlpha(0x8f), ModColors.MAIN_COLOR_M.withAlpha(0)));

		overview.addChild(new TextWidget(fromTopLeft(7, 44), Component.translatable("gui.arcanism.machine.label.machine_type")).bottomAlignY().applyTheme(LABEL_TEXT));
		overview.addChild(new TextWidget(fromTopLeft(7, 18), Component.translatable("gui.arcanism.machine.type.mana_buffer")).scaled(1.5));
		overview.addChild(new TextWidget(fromTopRight(-7, 44), Component.translatable("gui.arcanism.machine.label.mana_type")).rightAlign().bottomAlignY().applyTheme(LABEL_TEXT));
		overview.addChild(new TextWidget(fromTopRight(-7, 18), Component.translatable("gui.arcanism.machine.mana_type.momentum")).scaled(1.5).rightAlign());

		overview.addChild(new TextWidget(fromCenterBottom(0, -38), Component.translatable("gui.arcanism.machine.buffer.main_text.capacity")).centerAlign().bottomAlignY().mainColor(0xff7f7f7f));
		var unit = new TextSwitchWidget(fromCenterBottom(68, -6), fromTopLeft(26, 18), 2, "J");
		overview.addChild(unit.bottomAlignY().mainColor(-1));
		overview.addChild(new NumberDisplayWidget(fromCenterBottom(0, -6), totalCache, 6, 2.5, unit, "J", false).centerAlign().bottomAlignY());

		// ================= 存储窗口 ================= //

		var storageWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(-190, -20), fromTopLeft(190, 138), Component.translatable("gui.arcanism.machine.buffer.window.storage"), 0, 0));

		var colors = List.of(ModColors.MAIN_COLOR_R, ModColors.MAIN_COLOR_T, ModColors.MAIN_COLOR_M, ModColors.MAIN_COLOR_P);
		var storages = List.of(menu.radiationStorageJ, menu.temperatureStorageJ, menu.momentumStorageJ, menu.pressureStorageJ);
		var caches = List.of(menu.radiationCacheJ, menu.temperatureCacheJ, menu.momentumCacheJ, menu.pressureCacheJ);
		var names = List.of(
				Component.translatable("gui.arcanism.machine.mana_type.radiation"),
				Component.translatable("gui.arcanism.machine.mana_type.temperature"),
				Component.translatable("gui.arcanism.machine.mana_type.momentum"),
				Component.translatable("gui.arcanism.machine.mana_type.pressure"));

		for (int i = 0; i < 4; i++) {
			int y = 18 + i * 24;
			storageWindow.addChild(new RectangleWidget(fromTopLeft(7, y), fromTopLeft(4, 14)).mainColor(colors.get(i)));
			storageWindow.addChild(new TextWidget(fromTopLeft(14, y), names.get(i)).applyTheme(LABEL_TEXT));
			storageWindow.addChild(new NumberDisplayWidget(fromTopRight(-7, y - 1), storages.get(i), 6, 1, true).rightAlign().mainColor(-1));
			storageWindow.addChild(new ProgressBarWidget(fromTopLeft(14, y + 12), fromTopRight(-7, 5), storages.get(i), caches.get(i)).mainColor(colors.get(i)).bgColor(colors.get(i).withAlpha(0x1f)));
		}

		storageWindow.addChild(new TextWidget(fromTopLeft(7, 113), Component.translatable("gui.arcanism.machine.buffer.main_text.net_power")).applyTheme(LABEL_TEXT));
		storageWindow.addChild(new NumberDisplayWidget(fromTopRight(-7, 113), netPower, 6, 1, true).rightAlign().mainColor(-1));

		// ================= 充能控制窗口 ================= //

		var chargeWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(10, -20), fromTopLeft(190, 138), Component.translatable("gui.arcanism.machine.buffer.window.charge"), 0, 0));

		chargeWindow.addChild(new SwitchWidget(fromTopLeft(7, 16), fromTopLeft(70, 19), Component.translatable("gui.arcanism.machine.switch.off"), Component.translatable("gui.arcanism.machine.switch.on"), menu.enabled).onSwitch(enabled -> menu.powerSwitch.trigger(enabled)).mainColor(-1));

		chargeWindow.addChild(new TextWidget(fromTopRight(-7, 34), Component.translatable("gui.arcanism.machine.buffer.label.charge_power")).rightAlign().bottomAlignY().applyTheme(LABEL_TEXT));
		chargeWindow.addChild(new NumberDisplayWidget(fromTopRight(-7, 16), DynamicValue.fromSupplier(() -> menu.maxChargePower.get() * menu.powerFact.get()), 6, 1, true).rightAlign().mainColor(-1));

		chargeWindow.addChild(new TextWidget(fromTopLeft(7, 42), Component.translatable("gui.arcanism.machine.buffer.label.overclock_factor")).applyTheme(GENERAL_TEXT));
		chargeWindow.addChild(new NumberDisplayWidget(fromTopLeft(50, 38), menu.powerFact, 4, 1.5, true).mainColor(-1));
		chargeWindow.addChild(new ThinSlideBarWidget(fromTopLeft(7, 52), fromTopRight(-14, 5), 0, 5, menu.powerFact).step(0.05).bgColor(-1));

		chargeWindow.addChild(new TextWidget(fromTopLeft(7, 64), Component.translatable("gui.arcanism.machine.buffer.label.charge_slots")).applyTheme(LABEL_TEXT));
		menu.chargeSlotCount.whenFirstDataArrivesDo(() -> {
			for (int i = 0; i < menu.chargeSlotCount.get(); i++) {
				chargeWindow.addChild(new SlotWidget(menu.slots.get(menu.chargeSlotStart + i), fromTopLeft(7 + i * 20, 74)));
				chargeWindow.addChild(new OutlineWidget(fromTopLeft(7 + i * 20, 74), fromTopLeft(16, 16)).mainColor(0x1fffffff));
			}
		});

		// ================= 物品栏窗口 ================= //

		addWidget(new MachineWidgets.InventoryWindow(fromCenter(-90, 135), fromTopLeft(184, 110)));
	}
}
