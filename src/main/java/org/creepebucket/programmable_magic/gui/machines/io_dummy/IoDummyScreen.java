package org.creepebucket.programmable_magic.gui.machines.io_dummy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

import java.util.ArrayList;


import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.*;
import static net.minecraft.network.chat.Component.literal;

public class IoDummyScreen extends MachineScreen<IoDummyMenu> {
	public MachineWidgets.InformationWindowWidget contentWindow;

	public IoDummyScreen(IoDummyMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();
		addWidget(new MachineWidgets.InventoryWindow(fromCenter(-100, 5), fromTopLeft(200, 100)));

		contentWindow = new MachineWidgets.InformationWindowWidget(fromCenter(-100, -105),
				fromTopLeft(200, 100), literal("正在加载..."), 200, 100);
		addWidget(contentWindow);

		menu.ioType.whenFirstDataArrivesDo(this::buildWindowContent);
	}

	public void buildWindowContent() {
		contentWindow.name = Component.translatable("gui.programmable_magic.machine.io_dummy." + menu.ioType.get());
		contentWindow.children = new ArrayList<>(); // 实际上在不改变组件的情况下重建组件
		contentWindow.onInitialize();

		if (menu.ioType.get().startsWith("item")) {
			// 物品仓
			for (int i = -2; i <= 4; i += 2) for (int j = -3; j <= 3; j += 2) {
				contentWindow.addChild(new RectangleWidget(fromCenter(j * 9, i * 9 + 3), fromTopLeft(16, 1)).centerAlign().centerAlignY().mainColor(0x1fffffff));
				contentWindow.addChild(new SlotWidget(menu.slots.get(i * 2 + (j - 1) / 2 + 42), fromCenter(j * 9, i * 9 + 4)).centerAlign().bottomAlignY());
			}
		} else {
			// 流体仓
			var upper = contentWindow.addChild(new Widget.BlankWidget(fromTopLeft(0, 7), fromBottomRight(0, 0)));

			upper.addChild(new MachineWidgets.FluidInfoWidget(fromTopLeft(7, 15), menu.fluidId));

			upper.addChild(new TextWidget(fromCenterTop(4, 15), literal("流体存取")).noShadow().mainColor(0xff7f7f7f));
			upper.addChild(new SlotWidget(menu.slots.get(52), fromCenterTop(4, 27)));
			upper.addChild(new SlotWidget(menu.slots.get(53), fromCenterTop(40, 27)));
			upper.addChild(new OutlineWidget(fromCenterTop(4, 27), fromTopLeft(16, 16)).mainColor(0x0fffffff));
			upper.addChild(new OutlineWidget(fromCenterTop(40, 27), fromTopLeft(16, 16)).mainColor(0x0fffffff));
			upper.addChild(new TextWidget(fromCenterTop(11, 31), literal("I")).noShadow().mainColor(0x1fffffff));
			upper.addChild(new TextWidget(fromCenterTop(46, 31), literal("O")).noShadow().mainColor(0x1fffffff));
			upper.addChild(new TextWidget(fromCenterTop(26, 31), literal(">>")).noShadow().mainColor(0x1fffffff));

			var doubleAmount = DynamicValue.fromSupplier(() -> Double.valueOf(menu.fluidAmount.get()));
			var doubleCapacity = DynamicValue.fromSupplier(() -> Double.valueOf(menu.fluidCapacity.get()));
			var bottom = contentWindow.addChild(new Widget.BlankWidget(fromTopLeft(0, -5), fromBottomRight(0, 0)));

			bottom.addChild(new TextWidget(fromBottomLeft(7, -20), literal("流体存储")).noShadow().mainColor(0xff7f7f7f).bottomAlignY());
			bottom.addChild(new NumberDisplayWidget(fromCenterBottom(-11, -21), doubleAmount, 6, 1, true).rightAlign().bottomAlignY());
			bottom.addChild(new TextWidget(fromCenterBottom(-4, -20), literal("L")).noShadow().rightAlign().bottomAlignY());

			bottom.addChild(new TextWidget(fromCenterBottom(4, -20), literal("最大缓存")).noShadow().mainColor(0xff7f7f7f).bottomAlignY());
			bottom.addChild(new NumberDisplayWidget(fromBottomRight(-14, -21), doubleCapacity, 6, 1, true).rightAlign().bottomAlignY());
			bottom.addChild(new TextWidget(fromBottomRight(-7, -20), literal("L")).noShadow().rightAlign().bottomAlignY());

			bottom.addChild(new ProgressBarWidget(fromBottomLeft(7, -7), fromTopRight(-14, 9),doubleAmount, doubleCapacity).bottomAlignY());
		}
	}
}
