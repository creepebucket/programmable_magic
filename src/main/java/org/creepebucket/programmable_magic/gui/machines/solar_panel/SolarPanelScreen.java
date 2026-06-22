package org.creepebucket.programmable_magic.gui.machines.solar_panel;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;

public class SolarPanelScreen extends MachineScreen<SolarPanelMenu> {

	public SolarPanelScreen(SolarPanelMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		/*

		// =================== 计算细节 =================== //
		// .addDetailLine(Component.literal(""), , Component.literal(""), "");

		// (
		addWidget(new RectangleWidget(Coordinate.fromCenter(-198, -70), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);
		addWidget(new TextWidget(Coordinate.fromCenter(-196, -70), Component.literal("(")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .17);

		// 直接辐照度
		var directIrradianceWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-185, -70), Coordinate.fromTopLeft(83, 11), menu.directIrradiance,
				Component.literal("W/m^2"), Component.literal("直接辐照度")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .1);

		directIrradianceWidget.addDetailLine(Component.literal("天气修正"), menu.weatherFactDirect, Component.literal("云层会影响光线: 晴天最高, 雨天较低"), "x");
		directIrradianceWidget.addDetailLine(Component.literal("海拔修正"), menu.altitudeFact, Component.literal("更高的海拔能降低大气质量"), " ");
		directIrradianceWidget.addDetailLine(Component.literal("大气质量"), menu.airMass, Component.literal("原始值, 受时间(太阳高度角)影响"), " ");
		directIrradianceWidget.addDetailLine(Component.literal("太阳常数"), menu.solarConstant, Component.literal("固定值, 原始的太阳辐照度"), "+");

		// +
		addWidget(new RectangleWidget(Coordinate.fromCenter(-100, -70), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .05);
		addWidget(new TextWidget(Coordinate.fromCenter(-97, -70), Component.literal("+")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .07);

		// 散射辐照度
		var diffuseIrradianceWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-87, -70), Coordinate.fromTopLeft(84, 11), menu.diffuseIrradiance,
				Component.literal("W/m^2"), Component.literal("散射辐照度")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .0);

		diffuseIrradianceWidget.addDetailLine(Component.literal("天气修正"), menu.weatherFactDiffuse, Component.literal("云层会影响光线: 雨天最高, 晴天次之, 雷暴最低"), "x");
		diffuseIrradianceWidget.addDetailLine(Component.literal("海拔修正"), menu.altitudeFact, Component.literal("更高的海拔能降低大气质量"), " ");
		diffuseIrradianceWidget.addDetailLine(Component.literal("大气质量"), menu.airMass, Component.literal("原始值, 受时间(太阳高度角)影响"), " ");
		diffuseIrradianceWidget.addDetailLine(Component.literal("太阳常数"), menu.solarConstant, Component.literal("固定值, 原始的太阳辐照度"), "+");

		// )x
		addWidget(new RectangleWidget(Coordinate.fromCenter(-1, -70), Coordinate.fromTopLeft(17, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), 0);
		addWidget(new TextWidget(Coordinate.fromCenter(2, -70), Component.literal(")x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .02);

		// 风速
		var panelAreaWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(18, -70), Coordinate.fromTopLeft(83, 11), menu.panelArea, Component.literal("m^2"),
				Component.literal("受光面积")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .05);

		panelAreaWidget.addDetailLine(Component.literal("面积基准"), menu.solarConstant, Component.literal("该型号的受光面积"), "+");

		// x
		addWidget(new RectangleWidget(Coordinate.fromCenter(103, -70), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .1);
		addWidget(new TextWidget(Coordinate.fromCenter(106, -70), Component.literal("x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .12);

		// 功率系数
		var powerCoeffWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(116, -70), Coordinate.fromTopLeft(83, 11), menu.efficiencyFact,
				Component.literal("%"), Component.literal("转化效率")).mainColor(new Color(255, 255, 0)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);

		powerCoeffWidget.addDetailLine(Component.literal("材料系数"), menu.materialFact, Component.literal("该型号的材料系数"), "x");
		powerCoeffWidget.addDetailLine(Component.literal("温度系数"), menu.thermalFact, Component.literal("当太阳直射太阳能板时, 效率会因产生热量而小幅下降"), "x");
		powerCoeffWidget.addDetailLine(Component.literal("常数"), DynamicValue.staticValue(100d), Component.literal("常数"), "+");

		// =================== 功率显示 =================== //
		addWidget(new MachineWidgets.PowerDisplayWidget(Coordinate.fromCenter(-198, -58), menu, menu.power,
				new Color(255, 255, 0), Component.literal("辐射"),
				enabled -> menu.powerSwitch.trigger(enabled)));

		// 维护成本
		addWidget(new RectangleWidget(Coordinate.fromCenter(-131, 8), Coordinate.fromTopLeft(44, 20)).mainColor(new Color(0, 0, 0, 127)));
		addWidget(new TextWidget(Coordinate.fromCenter(-129, 10), Component.literal("维护成本::")).noShadow());
		addWidget(new TextWidget(Coordinate.fromCenter(-129, 19), Component.literal("0")) .noShadow().mainColor(new Color(255, 255, 0)));
		addWidget(new TextWidget(Coordinate.fromCenter(-119, 19), Component.literal("0")) .noShadow().mainColor(new Color(255,   0, 0)));
		addWidget(new TextWidget(Coordinate.fromCenter(-109, 19), Component.literal("10")).noShadow().mainColor(new Color(0, 255, 255)));
		addWidget(new TextWidget(Coordinate.fromCenter(-94 , 19), Component.literal("0")) .noShadow().mainColor(new Color(0, 255, 0  )));

		// =================== 网络状态 =================== //
		addWidget(new MachineWidgets.NetworkInfoWidget(Coordinate.fromCenter(10, -58), menu));

		// =================== 标题装饰 =================== //
		addWidget(new TextWidget(Coordinate.fromCenter(-198, 40), Component.translatable("gui.programmable_magic.machine.wind_turbine.title.machine_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
		addWidget(new TextWidget(Coordinate.fromCenter(10, 40), Component.translatable("gui.programmable_magic.machine.wind_turbine.title.network_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00); */
	}
}
