package org.creepebucket.programmable_magic.gui.machines.io_dummy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

import java.util.ArrayList;

public class IoDummyScreen extends MachineScreen<IoDummyMenu> {
	public MachineWidgets.InformationWindowWidget contentWindow;
	Widget.BlankWidget upperWidget;
	FluidTextureWidget fluidTexture;
	TextWidget fluidNameText;
	TextWidget fluidIdText;

	public IoDummyScreen(IoDummyMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();
		addWidget(new MachineWidgets.InventoryWindow(Coordinate.fromCenter(-100, 5), Coordinate.fromTopLeft(200, 100)));

		contentWindow = new MachineWidgets.InformationWindowWidget(Coordinate.fromCenter(-100, -105),
				Coordinate.fromTopLeft(200, 100), Component.literal("正在加载..."), 200, 100);
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
				contentWindow.addChild(new RectangleWidget(Coordinate.fromCenter(j * 9, i * 9 + 3), Coordinate.fromTopLeft(16, 1)).centerAlign().centerAlignY().mainColor(0x1fffffff));
				contentWindow.addChild(new SlotWidget(menu.slots.get(i * 2 + (j - 1) / 2 + 42), Coordinate.fromCenter(j * 9, i * 9 + 4)).centerAlign().bottomAlignY());
			}
		} else {
			// 流体仓
			var upper = contentWindow.addChild(new Widget.BlankWidget(Coordinate.fromTopLeft(0, 7), Coordinate.fromBottomRight(0, 0)));
			upperWidget = (Widget.BlankWidget) upper;

			upper.addChild(new TextWidget(Coordinate.fromTopLeft(7, 15), Component.literal("流体类型")).noShadow().mainColor(0xff7f7f7f));
			fluidTexture = (FluidTextureWidget) upper.addChild(new FluidTextureWidget(Coordinate.fromTopLeft(7, 27), Coordinate.fromTopLeft(10, 10), menu.fluidId.get()));
			fluidNameText = (TextWidget) upper.addChild(new TextWidget(Coordinate.fromTopLeft(20, 28), menu.fluidId.get().isEmpty() ? Component.literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.fluidId.get())).getFluidType().getDescriptionId())).noShadow());
			fluidIdText = (TextWidget) upper.addChild(new TextWidget(Coordinate.fromTopLeft(7, 40), Component.literal(menu.fluidId.get())).scaled(0.5).noShadow().mainColor(0xff7f7f7f));

			menu.fluidId.whenDataChangedDo(() -> {
					fluidTexture.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
					fluidNameText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
					fluidIdText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

					fluidTexture = (FluidTextureWidget) upperWidget.addChild(new FluidTextureWidget(Coordinate.fromTopLeft(7, 27), Coordinate.fromTopLeft(10, 10), menu.fluidId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
					fluidNameText = (TextWidget) upperWidget.addChild(new TextWidget(Coordinate.fromTopLeft(20, 28), menu.fluidId.get().isEmpty() ? Component.literal("") :
							Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.fluidId.get())).getFluidType().getDescriptionId())).noShadow().addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
					fluidIdText = (TextWidget) upperWidget.addChild(new TextWidget(Coordinate.fromTopLeft(7, 40), Component.literal(menu.fluidId.get())).scaled(0.5).noShadow().mainColor(0xff7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
				});

			upper.addChild(new TextWidget(Coordinate.fromCenterTop(4, 15), Component.literal("流体存取")).noShadow().mainColor(0xff7f7f7f));
			upper.addChild(new SlotWidget(menu.slots.get(52), Coordinate.fromCenterTop(4, 27)));
			upper.addChild(new SlotWidget(menu.slots.get(53), Coordinate.fromCenterTop(40, 27)));
			upper.addChild(new OutlineWidget(Coordinate.fromCenterTop(4, 27), Coordinate.fromTopLeft(16, 16)).mainColor(0x0fffffff));
			upper.addChild(new OutlineWidget(Coordinate.fromCenterTop(40, 27), Coordinate.fromTopLeft(16, 16)).mainColor(0x0fffffff));
			upper.addChild(new TextWidget(Coordinate.fromCenterTop(11, 31), Component.literal("I")).noShadow().mainColor(0x1fffffff));
			upper.addChild(new TextWidget(Coordinate.fromCenterTop(46, 31), Component.literal("O")).noShadow().mainColor(0x1fffffff));
			upper.addChild(new TextWidget(Coordinate.fromCenterTop(26, 31), Component.literal(">>")).noShadow().mainColor(0x1fffffff));

			var doubleAmount = DynamicValue.fromSupplier(() -> Double.valueOf(menu.fluidAmount.get()));
			var doubleCapacity = DynamicValue.fromSupplier(() -> Double.valueOf(menu.fluidCapacity.get()));
			var bottom = contentWindow.addChild(new Widget.BlankWidget(Coordinate.fromTopLeft(0, -5), Coordinate.fromBottomRight(0, 0)));

			bottom.addChild(new TextWidget(Coordinate.fromBottomLeft(7, -20), Component.literal("流体存储")).noShadow().mainColor(0xff7f7f7f).bottomAlignY());
			bottom.addChild(new NumberDisplayWidget(Coordinate.fromCenterBottom(-11, -21), doubleAmount, 6, 1, true).rightAlign().bottomAlignY());
			bottom.addChild(new TextWidget(Coordinate.fromCenterBottom(-4, -20), Component.literal("L")).noShadow().rightAlign().bottomAlignY());

			bottom.addChild(new TextWidget(Coordinate.fromCenterBottom(4, -20), Component.literal("最大缓存")).noShadow().mainColor(0xff7f7f7f).bottomAlignY());
			bottom.addChild(new NumberDisplayWidget(Coordinate.fromBottomRight(-14, -21), doubleCapacity, 6, 1, true).rightAlign().bottomAlignY());
			bottom.addChild(new TextWidget(Coordinate.fromBottomRight(-7, -20), Component.literal("L")).noShadow().rightAlign().bottomAlignY());

			bottom.addChild(new ProgressBarWidget(Coordinate.fromBottomLeft(7, -7), Coordinate.fromTopRight(-14, 9),doubleAmount, doubleCapacity).bottomAlignY());
		}
	}
}
