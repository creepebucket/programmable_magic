package org.creepebucket.programmable_magic.gui.machines.consumer.water_pump;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;
import org.creepebucket.programmable_magic.utils.ModColors;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.fromCenter;
import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.fromTopLeft;

public class WaterPumpScreen extends MachineScreen<WaterPumpMenu> {

	public WaterPumpScreen(WaterPumpMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_M);

		addWidget(new MachineWidgets.MachineInfoWindow(fromCenter(-125, -70), fromTopLeft(250, 80), menu.power, literal("动量/Mom"),
				literal("水泵"), literal("[每秒流量]"), literal("Q="), "L"));

		addWidget(new MachineWidgets.OverclockWindow(fromCenter(-125, 20), fromTopLeft(170, 50), menu.powerFact, 2000d, 300d, 4d));

		addWidget(new MachineWidgets.MachineControlWindow(fromCenter(55, 20), fromTopLeft(70, 50), menu));

		var w = addWidget(new MachineWidgets.NetworkInfoWindow(fromCenter(-90, -50), fromTopLeft(180, 100), menu));
		w.enabled = false; // 不能.disable();

		var info = new MachineWidgets.PowerInfoWindow(fromCenter(-90, -40), fromTopLeft(180, 80), literal("总流量 = Q₀×k"));
		addWidget(info.disable());
		info.addPowerInfoItem(literal("基础流量/Q₀"), DynamicValue.staticValue(1000d), literal("L/s"));
		info.addPowerInfoItem(literal("超频倍率/k"), menu.powerFact, literal("x"));

	}
}
