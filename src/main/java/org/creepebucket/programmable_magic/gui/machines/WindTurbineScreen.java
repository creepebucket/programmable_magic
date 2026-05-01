package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

public class WindTurbineScreen extends Screen<WindTurbineMenu> {
    public MachineWidgets.TextSwitchWidget powerUnit;

    public WindTurbineScreen(WindTurbineMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        // 计算组件
        addWidget(new TextWidget(Coordinate.fromCenter(-239, -33), Component.literal("0.5x")).addAnimation(new Animation.FadeIn.FromTop(0.5), .15));
        addWidget(new TextWidget(Coordinate.fromCenter(-114, -33), Component.literal("x")).addAnimation(new Animation.FadeIn.FromTop(0.5), .05));
        addWidget(new TextWidget(Coordinate.fromCenter(-3, -33), Component.literal("x")).addAnimation(new Animation.FadeIn.FromTop(0.5), 0));
        addWidget(new TextWidget(Coordinate.fromCenter(107, -33), Component.literal("^3x")).addAnimation(new Animation.FadeIn.FromTop(0.5), .05));

        // 空气密度
        var airDensityWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(
                Coordinate.fromCenter(-217, -35), Coordinate.fromTopLeft(100, 11), menu.airDensity, Component.literal("kg/m^3"), Component.translatable("gui.programmable_magic.machine.wind_turbine.air_density")).addAnimation(new Animation.FadeIn.FromTop(0.5), .1));

        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.humidity_factor"), menu.airDensityHumidFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.humidity_factor"));
        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.temperature_factor"), menu.airDensityTempFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.temperature_factor"));
        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.pressure_factor"), menu.airDensityPressureFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.pressure_factor"));
        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_density"), menu.airDensityBase, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.base_density"));


        // 扫风面积
        var sweptAreaWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(
                Coordinate.fromCenter(-106, -35), Coordinate.fromTopLeft(100, 11), new SyncedValue.StaticDouble(6.0), Component.literal("m^2"), Component.translatable("gui.programmable_magic.machine.wind_turbine.swept_area")).addAnimation(new Animation.FadeIn.FromTop(0.5), 0));

        sweptAreaWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_area"), new SyncedValue.StaticDouble(6.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.swept_area.base_area"));

        // 风速
        var airSpeedDetailWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(
                Coordinate.fromCenter(5, -35), Coordinate.fromTopLeft(100, 11), menu.windSpeed, Component.literal("m/s"), Component.translatable("gui.programmable_magic.machine.wind_turbine.wind_speed")).addAnimation(new Animation.FadeIn.FromTop(0.5), 0));

        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.weather_factor"), menu.windSpeedWeatherFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.weather_factor"));
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.time_factor"), menu.windSpeedTimeFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.time_factor"));
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.altitude_factor"), menu.windSpeedAltitudeFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.altitude_factor"));
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.wind_shear_exponent"), menu.windShearExponent, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.wind_shear_exponent"));
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_speed"), menu.windSpeedBase, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.base_speed"));

        // 功率系数
        var powerCoeffWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(
                Coordinate.fromCenter(126, -35), Coordinate.fromTopLeft(100, 11), new SyncedValue.StaticDouble(25.0), Component.literal("%"), Component.translatable("gui.programmable_magic.machine.wind_turbine.power_coefficient")).addAnimation(new Animation.FadeIn.FromTop(0.5), .1));

        powerCoeffWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_coefficient"), new SyncedValue.StaticDouble(25.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.power_coefficient.base_coefficient"));

        // 功率计
        powerUnit = (MachineWidgets.TextSwitchWidget) addWidget(new MachineWidgets.TextSwitchWidget(Coordinate.fromCenter(121, -22), Coordinate.fromTopRight(0,27), 3, "W").addAnimation(new Animation.FadeIn.FromRight(0.5), 0));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(-118, -22), menu.power, 6, 5, powerUnit, "W", false).addAnimation(new Animation.FadeIn.FromBottom(0.5), 0));

        addWidget(new RectangleWidget(Coordinate.fromCenterLeft(0, -22), Coordinate.fromCenterTop(-122, 27)).mainColor(new Color(0x80000000)).addAnimation(new Animation.FadeIn.FromLeft(0.5), 0));
        addWidget(new TextWidget(Coordinate.fromCenter(-158, -19), Component.literal("P=")).scaled(3).noShadow().addAnimation(new Animation.FadeIn.FromLeft(0.5), 0.1));
        addWidget(new TextWidget(Coordinate.fromCenter(123, 9), Component.literal("Momentum")).scaled(2).mainColor(new Color(0xFF00FFFF)).addAnimation(new Animation.FadeIn.FromRight(0.5), 0.1));

        //addWidget(new MouseCursorWidget());
    }
}
