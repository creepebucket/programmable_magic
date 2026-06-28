
package org.creepebucket.programmable_magic.gui.machines.generator.wind_turbine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

public class WindTurbineScreen extends MachineScreen<WindTurbineMenu> {

	public WindTurbineScreen(WindTurbineMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(0xff00ffff);

		// 主要信息显示
		addWidget(new MachineWidgets.MachineInfoWindow(Coordinate.fromCenter(-200, -90), Coordinate.fromTopLeft(210, 80), menu.power, Component.literal("动量/Mom"),
				Component.literal("风力涡轮机"), Component.literal("[输出功率]"), Component.literal("P="), "W"));

		// 功率计算
		var calculationsWindow = new MachineWidgets.PowerInfoWindow(Coordinate.fromCenter(-200, 0), Coordinate.fromTopLeft(210, 90), Component.literal("总功率 = 0.5×ρ×s×v³×η"));
		addWidget(calculationsWindow);

		calculationsWindow.addPowerInfoItem(Component.literal("空气密度/ρ"), menu.airDensity, Component.literal("kg/m³"));
		calculationsWindow.addPowerInfoItem(Component.literal("风速/v"), menu.windSpeed, Component.literal("m/s"));
		calculationsWindow.addPowerInfoItem(Component.literal("扫风面积/s"), DynamicValue.staticValue(6d), Component.literal("m²"));
		calculationsWindow.addPowerInfoItem(Component.literal("转换效率/η"), DynamicValue.staticValue(25d), Component.literal("%"));

		addWidget(new MachineWidgets.NetworkInfoWindow(Coordinate.fromCenter(20, -90), Coordinate.fromTopLeft(180, 120), menu));

		addWidget(new MachineWidgets.MachineControlWindow(Coordinate.fromCenter(20, 40), Coordinate.fromTopLeft(180, 50), menu));

		// 空气密度
		/*var exprAirDensity = addWidget(new Widget.BlankWidget(Coordinate.fromCenter(-193, -40), Coordinate.ZERO));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(Coordinate.fromTopLeft(0, 0), DynamicValue.staticValue(1.225), 6, 1, true));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(1, -10), Component.literal("密度基准值")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(38, 1), Component.literal("×")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(Coordinate.fromTopLeft(44, 0), menu.airDensityPressureFact, 6, 1, true));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(45, 11), Component.literal("海拔修正")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(82, 1), Component.literal("×")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(Coordinate.fromTopLeft(88, 0), menu.airDensityHumidFact, 6, 1, true));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(89, -10), Component.literal("湿度修正")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(126, 1), Component.literal("×")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(Coordinate.fromTopLeft(132, 0), menu.airDensityTempFact, 6, 1, true));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(133, 11), Component.literal("温度修正")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(170, 1), Component.literal("=")).noShadow().mainColor(0xffbfbfbf));
		exprAirDensity.addChild(new MachineWidgets.NumberDisplayWidget(Coordinate.fromTopLeft(176, 0), menu.airDensity, 6, 1, true).mainColor(0xff00ffff));
		exprAirDensity.addChild(new TextWidget(Coordinate.fromTopLeft(177, -10), Component.literal("空气密度")).noShadow());*/
	}
}
