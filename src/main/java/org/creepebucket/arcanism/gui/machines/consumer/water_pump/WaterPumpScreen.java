package org.creepebucket.arcanism.gui.machines.consumer.water_pump;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.machines.api.MachineScreen;
import org.creepebucket.arcanism.gui.machines.api.MachineWidgets;
import org.creepebucket.arcanism.utils.ModColors;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.arcanism.gui.lib.api.Coordinate.fromCenter;
import static org.creepebucket.arcanism.gui.lib.api.Coordinate.fromTopLeft;

public class WaterPumpScreen extends MachineScreen<WaterPumpMenu> {

	public WaterPumpScreen(WaterPumpMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_M);

		addWidget(new MachineWidgets.MachineInfoWindow(fromCenter(-125, -70), fromTopLeft(250, 80), menu.power, Component.translatable("gui.arcanism.machine.mana_type.momentum"),
				Component.translatable("gui.arcanism.machine.type.water_pump"), Component.translatable("gui.arcanism.machine.main_text.flow_per_second"), literal("Q="), "L"));

		addWidget(new MachineWidgets.OverclockWindow(fromCenter(-125, 20), fromTopLeft(170, 50), menu.powerFact, 2000d, 300d, 4d));

		addWidget(new MachineWidgets.MachineControlWindow(fromCenter(55, 20), fromTopLeft(70, 50), menu));

		var w = addWidget(new MachineWidgets.NetworkInfoWindow(fromCenter(-90, -50), fromTopLeft(180, 100), menu));
		w.enabled = false; // 不能.disable();

		var info = new MachineWidgets.PowerInfoWindow(fromCenter(-90, -40), fromTopLeft(180, 80), Component.translatable("gui.arcanism.machine.water_pump.power_expr"));
		addWidget(info.disable());
		info.addPowerInfoItem(Component.translatable("gui.arcanism.machine.water_pump.base_flow_item"), DynamicValue.staticValue(1000d), literal("L/s"));
		info.addPowerInfoItem(Component.translatable("gui.arcanism.machine.water_pump.overclock_item"), menu.powerFact, literal("x"));

	}
}
