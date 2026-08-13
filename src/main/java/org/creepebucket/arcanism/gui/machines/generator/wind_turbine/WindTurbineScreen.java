
package org.creepebucket.arcanism.gui.machines.generator.wind_turbine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.machines.api.MachineScreen;
import org.creepebucket.arcanism.gui.machines.api.MachineWidgets;
import org.creepebucket.arcanism.utils.ModColors;


import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;
import static net.minecraft.network.chat.Component.literal;

public class WindTurbineScreen extends MachineScreen<WindTurbineMenu> {

	public WindTurbineScreen(WindTurbineMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_M);

		// 主要信息显示
		addWidget(new MachineWidgets.MachineInfoWindow(fromCenter(-200, -90), fromTopLeft(210, 80), menu.power, Component.translatable("gui.arcanism.machine.mana_type.momentum"),
				Component.translatable("gui.arcanism.machine.type.wind_turbine"), Component.translatable("gui.arcanism.machine.main_text.output_power"), literal("P="), "W"));

		// 功率计算
		var calculationsWindow = new MachineWidgets.PowerInfoWindow(fromCenter(-200, 0), fromTopLeft(210, 90), Component.translatable("gui.arcanism.machine.wind_turbine.power_expr"));
		addWidget(calculationsWindow);

		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.wind_turbine.air_density_item"), menu.airDensity, literal("kg/m³"));
		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.wind_turbine.wind_speed_item"), menu.windSpeed, literal("m/s"));
		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.wind_turbine.swept_area_item"), DynamicValue.staticValue(6d), literal("m²"));
		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.wind_turbine.efficiency_item"), DynamicValue.staticValue(25d), literal("%"));

		addWidget(new MachineWidgets.NetworkInfoWindow(fromCenter(20, -90), fromTopLeft(180, 120), menu));

		addWidget(new MachineWidgets.MachineControlWindow(fromCenter(20, 40), fromTopLeft(180, 50), menu));

		// 空气密度
		/*var exprAirDensity = addWidget(new Widget.BlankWidget(fromCenter(-193, -40), ZERO));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(fromTopLeft(0, 0), DynamicValue.staticValue(1.225), 6, 1, true));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(1, -10), literal("密度基准值")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(38, 1), literal("×")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(fromTopLeft(44, 0), menu.airDensityPressureFact, 6, 1, true));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(45, 11), literal("海拔修正")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(82, 1), literal("×")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(fromTopLeft(88, 0), menu.airDensityHumidFact, 6, 1, true));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(89, -10), literal("湿度修正")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(126, 1), literal("×")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(fromTopLeft(132, 0), menu.airDensityTempFact, 6, 1, true));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(133, 11), literal("温度修正")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(170, 1), literal("=")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(fromTopLeft(176, 0), menu.airDensity, 6, 1, true).mainColor(ModColors.MAIN_COLOR_M));
		exprAirDensity.addChild(new TextWidget(fromTopLeft(177, -10), literal("空气密度")).noShadow());*/
	}
}
