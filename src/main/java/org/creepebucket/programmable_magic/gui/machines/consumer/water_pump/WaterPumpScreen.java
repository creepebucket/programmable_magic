package org.creepebucket.programmable_magic.gui.machines.consumer.water_pump;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

public class WaterPumpScreen extends MachineScreen<WaterPumpMenu> {

	public WaterPumpScreen(WaterPumpMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(0xff00ffff);

		addWidget(new MachineWidgets.MachineInfoWindow(Coordinate.fromCenter(-125, -70), Coordinate.fromTopLeft(250, 80), menu.power, Component.literal("动量/Mom"),
				Component.literal("水泵"), Component.literal("[每秒流量]"), Component.literal("Q="), "L"));

		addWidget(new MachineWidgets.OverclockWindow(Coordinate.fromCenter(-125, 20), Coordinate.fromTopLeft(170, 50), menu.powerFact, 2000d, 300d, 4d));

		addWidget(new MachineWidgets.MachineControlWindow(Coordinate.fromCenter(55, 20), Coordinate.fromTopLeft(70, 50), menu));

		addWidget(new MachineWidgets.NetworkInfoWindow(Coordinate.fromCenter(-90, -50), Coordinate.fromTopLeft(180, 100), menu).disable());

		var info = new MachineWidgets.PowerInfoWindow(Coordinate.fromCenter(-90, -40), Coordinate.fromTopLeft(180, 80), Component.literal("总流量 = Q₀×k"));
		addWidget(info.disable());
		info.addPowerInfoItem(Component.literal("基础流量/Q₀"), DynamicValue.staticValue(1000d), Component.literal("L/s"));
		info.addPowerInfoItem(Component.literal("超频倍率/k"), menu.powerFact, Component.literal("x"));

	}
}
