
package org.creepebucket.programmable_magic.gui.machines.generator.wind_turbine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;
import org.creepebucket.programmable_magic.utils.ModColors;


import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.*;
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
		addWidget(new MachineWidgets.MachineInfoWindow(fromCenter(-200, -90), fromTopLeft(210, 80), menu.power, literal("动量/Mom"),
				literal("风力涡轮机"), literal("[输出功率]"), literal("P="), "W"));

		// 功率计算
		var calculationsWindow = new MachineWidgets.PowerInfoWindow(fromCenter(-200, 0), fromTopLeft(210, 90), literal("总功率 = 0.5×ρ×s×v³×η"));
		addWidget(calculationsWindow);

		calculationsWindow.addPowerInfoItem(literal("空气密度/ρ"), menu.airDensity, literal("kg/m³"));
		calculationsWindow.addPowerInfoItem(literal("风速/v"), menu.windSpeed, literal("m/s"));
		calculationsWindow.addPowerInfoItem(literal("扫风面积/s"), DynamicValue.staticValue(6d), literal("m²"));
		calculationsWindow.addPowerInfoItem(literal("转换效率/η"), DynamicValue.staticValue(25d), literal("%"));

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
