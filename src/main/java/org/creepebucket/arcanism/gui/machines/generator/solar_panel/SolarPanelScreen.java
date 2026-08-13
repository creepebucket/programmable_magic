package org.creepebucket.arcanism.gui.machines.generator.solar_panel;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.machines.api.MachineScreen;
import org.creepebucket.arcanism.gui.machines.api.MachineWidgets;
import org.creepebucket.arcanism.utils.ModColors;


import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;
import static net.minecraft.network.chat.Component.literal;

public class SolarPanelScreen extends MachineScreen<SolarPanelMenu> {

	public SolarPanelScreen(SolarPanelMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_R);

		addWidget(new MachineWidgets.MachineInfoWindow(fromCenter(-200, -90), fromTopLeft(210, 80), menu.power, Component.translatable("gui.arcanism.machine.mana_type.radiation"),
				Component.translatable("gui.arcanism.machine.type.solar_panel"), Component.translatable("gui.arcanism.machine.main_text.output_power"), literal("P="), "W"));

		var calculationsWindow = new MachineWidgets.PowerInfoWindow(fromCenter(-200, 0), fromTopLeft(210, 90), Component.translatable("gui.arcanism.machine.solar_panel.power_expr"));
		addWidget(calculationsWindow);

		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.solar_panel.direct_item"), menu.directIrradiance, literal("W/m²"));
		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.solar_panel.diffuse_item"), menu.diffuseIrradiance, literal("W/m²"));
		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.solar_panel.area_item"), menu.panelArea, literal("m²"));
		calculationsWindow.addPowerInfoItem(Component.translatable("gui.arcanism.machine.solar_panel.efficiency_item"), menu.efficiencyFact, literal("%"));

		addWidget(new MachineWidgets.NetworkInfoWindow(fromCenter(20, -90), fromTopLeft(180, 120), menu));

		addWidget(new MachineWidgets.MachineControlWindow(fromCenter(20, 40), fromTopLeft(180, 50), menu));

		/*
			// =================== 计算细节 =================== //
		// .addDetailLine(literal(""), , literal(""), "");

		// (
		addWidget(new RectangleWidget(fromCenter(-198, -70), fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);
		addWidget(new TextWidget(fromCenter(-196, -70), literal("(")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .17);

		// 直接辐照度
		var directIrradianceWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(fromCenter(-185, -70), fromTopLeft(83, 11), menu.directIrradiance,
				literal("W/m^2"), literal("直接辐照度")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .1);

		directIrradianceWidget.addDetailLine(literal("天气修正"), menu.weatherFactDirect, literal("云层会影响光线: 晴天最高, 雨天较低"), "x");
		directIrradianceWidget.addDetailLine(literal("海拔修正"), menu.altitudeFact, literal("更高的海拔能降低大气质量"), " ");
		directIrradianceWidget.addDetailLine(literal("大气质量"), menu.airMass, literal("原始值, 受时间(太阳高度角)影响"), " ");
		directIrradianceWidget.addDetailLine(literal("太阳常数"), menu.solarConstant, literal("固定值, 原始的太阳辐照度"), "+");

		// +
		addWidget(new RectangleWidget(fromCenter(-100, -70), fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .05);
		addWidget(new TextWidget(fromCenter(-97, -70), literal("+")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .07);

		// 散射辐照度
		var diffuseIrradianceWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(fromCenter(-87, -70), fromTopLeft(84, 11), menu.diffuseIrradiance,
				literal("W/m^2"), literal("散射辐照度")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .0);

		diffuseIrradianceWidget.addDetailLine(literal("天气修正"), menu.weatherFactDiffuse, literal("云层会影响光线: 雨天最高, 晴天次之, 雷暴最低"), "x");
		diffuseIrradianceWidget.addDetailLine(literal("海拔修正"), menu.altitudeFact, literal("更高的海拔能降低大气质量"), " ");
		diffuseIrradianceWidget.addDetailLine(literal("大气质量"), menu.airMass, literal("原始值, 受时间(太阳高度角)影响"), " ");
		diffuseIrradianceWidget.addDetailLine(literal("太阳常数"), menu.solarConstant, literal("固定值, 原始的太阳辐照度"), "+");

		// )x
		addWidget(new RectangleWidget(fromCenter(-1, -70), fromTopLeft(17, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), 0);
		addWidget(new TextWidget(fromCenter(2, -70), literal(")x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .02);

		// 风速
		var panelAreaWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(fromCenter(18, -70), fromTopLeft(83, 11), menu.panelArea, literal("m^2"),
				literal("受光面积")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .05);

		panelAreaWidget.addDetailLine(literal("面积基准"), menu.solarConstant, literal("该型号的受光面积"), "+");

		// x
		addWidget(new RectangleWidget(fromCenter(103, -70), fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .1);
		addWidget(new TextWidget(fromCenter(106, -70), literal("x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .12);

		// 功率系数
		var powerCoeffWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(fromCenter(116, -70), fromTopLeft(83, 11), menu.efficiencyFact,
				literal("%"), literal("转化效率")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);

		powerCoeffWidget.addDetailLine(literal("材料系数"), menu.materialFact, literal("该型号的材料系数"), "x");
		powerCoeffWidget.addDetailLine(literal("温度系数"), menu.thermalFact, literal("当太阳直射太阳能板时, 效率会因产生热量而小幅下降"), "x");
		powerCoeffWidget.addDetailLine(literal("常数"), DynamicValue.staticValue(100d), literal("常数"), "+");

		// =================== 功率显示 =================== //
		addWidget(new MachineWidgets.PowerDisplayWidget(fromCenter(-198, -58), menu, menu.power,
				new Color(255, 255, 0), literal("辐射"),
				enabled -> menu.powerSwitch.trigger(enabled)));

		// 维护成本
		addWidget(new RectangleWidget(fromCenter(-131, 8), fromTopLeft(44, 20)).mainColor(new Color(0, 0, 0, 127)));
		addWidget(new TextWidget(fromCenter(-129, 10), literal("维护成本::")).noShadow());
		addWidget(new TextWidget(fromCenter(-129, 19), literal("0")) .noShadow().mainColor(new Color(255, 255, 0)));
		addWidget(new TextWidget(fromCenter(-119, 19), literal("0")) .noShadow().mainColor(new Color(255,   0, 0)));
		addWidget(new TextWidget(fromCenter(-109, 19), literal("10")).noShadow().mainColor(new Color(0, 255, 255)));
		addWidget(new TextWidget(fromCenter(-94 , 19), literal("0")) .noShadow().mainColor(new Color(0, 255, 0  )));

		// =================== 网络状态 =================== //
		addWidget(new MachineWidgets.NetworkInfoWidget(fromCenter(10, -58), menu));

		// =================== 标题装饰 =================== //
		addWidget(new TextWidget(fromCenter(-198, 40), Component.translatable("gui.arcanism.machine.wind_turbine.title.machine_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
		addWidget(new TextWidget(fromCenter(10, 40), Component.translatable("gui.arcanism.machine.wind_turbine.title.network_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
		*/
	}
}
