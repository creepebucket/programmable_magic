package org.creepebucket.programmable_magic.gui.machines.wind_turbine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

public class WindTurbineScreen extends MachineScreen<WindTurbineMenu> {

	public WindTurbineScreen(WindTurbineMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	protected void init() {
		super.init();

		// =================== 计算细节 =================== //
		// 0.5x
		addWidget(new RectangleWidget(Coordinate.fromCenter(-198, -70), Coordinate.fromTopLeft(23, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);
		addWidget(new TextWidget(Coordinate.fromCenter(-196, -70), Component.literal("0.5x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .17);

		// 空气密度
		var airDensityWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-173, -70), Coordinate.fromTopLeft(79, 11), menu.airDensity,
				Component.literal("kg/m^3"), Component.translatable("gui.programmable_magic.machine.wind_turbine.air_density")).mainColor(new Color(0, 255, 255)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .1);

		airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.humidity_factor"), menu.airDensityHumidFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.humidity_factor"), "x");
		airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.temperature_factor"), menu.airDensityTempFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.temperature_factor"), "x");
		airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.pressure_factor"), menu.airDensityPressureFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.pressure_factor"), "x");
		airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_density"), menu.airDensityBase, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.base_density"), "+");

		// x
		addWidget(new RectangleWidget(Coordinate.fromCenter(-92, -70), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .05);
		addWidget(new TextWidget(Coordinate.fromCenter(-89, -70), Component.literal("x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .07);

		// 扫风面积
		var sweptAreaWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-80, -70), Coordinate.fromTopLeft(78, 11), new DynamicValue.StaticDouble(6.0),
				Component.literal("m^2"), Component.translatable("gui.programmable_magic.machine.wind_turbine.swept_area")).mainColor(new Color(0, 255, 255)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .0);

		sweptAreaWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_area"), new DynamicValue.StaticDouble(6.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.swept_area.base_area"), "+");

		// x
		addWidget(new RectangleWidget(Coordinate.fromCenter(1, -70), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), 0);
		addWidget(new TextWidget(Coordinate.fromCenter(4, -70), Component.literal("x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .02);

		// 风速
		var airSpeedDetailWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(14, -70), Coordinate.fromTopLeft(79, 11), menu.windSpeed, Component.literal("m/s"),
				Component.translatable("gui.programmable_magic.machine.wind_turbine.wind_speed")).mainColor(new Color(0, 255, 255)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .05);

		airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.weather_factor"), menu.windSpeedWeatherFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.weather_factor"), "x");
		airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.time_factor"), menu.windSpeedTimeFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.time_factor"), "x");
		airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.altitude_factor"), menu.windSpeedAltitudeFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.altitude_factor"), "x");
		airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.wind_shear_exponent"), menu.windShearExponent, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.wind_shear_exponent"), " ");
		airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_speed"), menu.windSpeedBase, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.base_speed"), "+");

		// ^3x
		addWidget(new RectangleWidget(Coordinate.fromCenter(95, -70), Coordinate.fromTopLeft(23, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .1);
		addWidget(new TextWidget(Coordinate.fromCenter(97, -70), Component.literal("^3x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .12);

		// 功率系数
		var powerCoeffWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(120, -70), Coordinate.fromTopLeft(78, 11), new DynamicValue.StaticDouble(25.0),
				Component.literal("%"), Component.translatable("gui.programmable_magic.machine.wind_turbine.power_coefficient")).mainColor(new Color(0, 255, 255)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);

		powerCoeffWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_coefficient"), new DynamicValue.StaticDouble(25.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.power_coefficient.base_coefficient"), "+");

		// =================== 功率显示 =================== //
		addWidget(new MachineWidgets.PowerDisplayWidget(Coordinate.fromCenter(-198, -58), menu, menu.power,
				new Color(0, 255, 255),
				Component.translatable("gui.programmable_magic.machine.wind_turbine.section.momentum"),
				enabled -> WindTurbineHooks.onSwitch(menu, enabled)));

		// =================== 网络状态 =================== //
		addWidget(new MachineWidgets.NetworkInfoWidget(Coordinate.fromCenter(10, -58), menu));

		// =================== 标题装饰 =================== //
		addWidget(new TextWidget(Coordinate.fromCenter(-198, 40), Component.translatable("gui.programmable_magic.machine.wind_turbine.title.machine_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
		addWidget(new TextWidget(Coordinate.fromCenter(10, 40), Component.translatable("gui.programmable_magic.machine.wind_turbine.title.network_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
	}
}
