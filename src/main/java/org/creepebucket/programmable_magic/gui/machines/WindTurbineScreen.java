package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.ProgressBarWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SwitchWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

public class WindTurbineScreen extends Screen<WindTurbineMenu> {
    public MachineWidgets.TextSwitchWidget powerUnit;
    public SwitchWidget powerSwitch;
    private boolean synced_enabled;
    private boolean initial_enabled;
    private boolean interacted;

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
        var sweptAreaWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-80, -70), Coordinate.fromTopLeft(78, 11), new SyncedValue.StaticDouble(6.0),
                Component.literal("m^2"), Component.translatable("gui.programmable_magic.machine.wind_turbine.swept_area")).mainColor(new Color(0, 255, 255)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .0);

        sweptAreaWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_area"), new SyncedValue.StaticDouble(6.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.swept_area.base_area"), "+");

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
        var powerCoeffWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(120, -70), Coordinate.fromTopLeft(78, 11), new SyncedValue.StaticDouble(25.0),
                Component.literal("%"), Component.translatable("gui.programmable_magic.machine.wind_turbine.power_coefficient")).mainColor(new Color(0, 255, 255)).bottomAlignY()).addAnimation(new Animation.FadeIn.FromTop(0.5), .15);

        powerCoeffWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_coefficient"), new SyncedValue.StaticDouble(25.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.power_coefficient.base_coefficient"), "+");

        // =================== 功率显示 =================== //

        // 装饰
        addWidget(new RectangleWidget(Coordinate.fromCenter(-198, -58), Coordinate.fromTopLeft(28, 20)).mainColor(new Color(0, 255, 255, 127))).addAnimation(new Animation.FadeIn.FromLeft(0.5), .22);
        addWidget(new TextWidget(Coordinate.fromCenter(-195, -55), Component.literal("P=")).scaled(2).noShadow()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .2);

        addWidget(new RectangleWidget(Coordinate.fromCenter(-10, -58), Coordinate.fromTopLeft(156, 20)).mainColor(new Color(0, 0, 0, 127)).rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .15);
        addWidget(new TextWidget(Coordinate.fromCenter(-11, -55), Component.translatable("gui.programmable_magic.machine.wind_turbine.section.momentum")).scaled(2).noShadow().rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .1);
        addWidget(new TextWidget(Coordinate.fromCenter(-163, -39), Component.translatable("gui.programmable_magic.machine.wind_turbine.section.type")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127))).addAnimation(new Animation.FadeIn.FromLeft(0.5), .05);

        // 功率单位
        var mainPowerUnit = new MachineWidgets.TextSwitchWidget(Coordinate.fromCenter(-10, 8), Coordinate.fromTopLeft(28, 18), 2, "W");
        addWidget(mainPowerUnit.rightAlign().mainColor(new Color(-1)).bgColor(new Color(255, 255, 255, 127))).addAnimation(new Animation.FadeIn.FromLeft(0.5), .1);
        addWidget(new RectangleWidget(Coordinate.fromCenter(-10, 26), Coordinate.fromTopLeft(28, 2)).mainColor(new Color(-1)).rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .12);

        // 主功率显示
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(-10, 0), menu.power, 6, 4, mainPowerUnit, "W", false).mainColor(new Color(0, 255, 255)).rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromLeft(0.5), 0);
        for (int i = 0; i < 6; i++) addWidget(new RectangleWidget(Coordinate.fromCenter(-i * 32 - 10, 0), Coordinate.fromTopLeft(28, 2)).mainColor(new Color(0, 255, 255)).rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .02 * i);

        // 开关
        powerSwitch = (SwitchWidget) addWidget(new SwitchWidget(Coordinate.fromCenter(-198, 8), Coordinate.fromTopLeft(60, 20), CommonComponents.OPTION_OFF, CommonComponents.OPTION_ON)
                .setPressed(menu.enabled.get()).onSwitch(enabled -> { interacted = true; WindTurbineHooks.onSwitch(menu, enabled);}).addAnimation(new Animation.FadeIn.FromLeft(0.5), .2));

        initial_enabled = menu.enabled.get();
        synced_enabled = false;
        interacted = false;

        // =================== 网络状态 =================== //

        //.mainColor(new Color(255, 255, 0))
        //.mainColor(new Color(255,   0, 0))
        //.mainColor(new Color(0, 255, 255))
        //.mainColor(new Color(0, 255, 0  ))

        addWidget(new RectangleWidget(Coordinate.fromCenter(10 , -58), Coordinate.fromTopLeft(46, 9)).mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
        addWidget(new RectangleWidget(Coordinate.fromCenter(57 , -58), Coordinate.fromTopLeft(46, 9)).mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .05);
        addWidget(new RectangleWidget(Coordinate.fromCenter(104, -58), Coordinate.fromTopLeft(46, 9)).mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .10);
        addWidget(new RectangleWidget(Coordinate.fromCenter(151, -58), Coordinate.fromTopLeft(47, 9)).mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);

        addWidget(new TextWidget(Coordinate.fromCenter(11 , -57), Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.radiation"))  .noShadow()).mainColor(new Color(255, 255, 0)).addAnimation(new Animation.FadeIn.FromRight(0.5), .02);
        addWidget(new TextWidget(Coordinate.fromCenter(58 , -57), Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.temperature")).noShadow()).mainColor(new Color(255,   0, 0)).addAnimation(new Animation.FadeIn.FromRight(0.5), .07);
        addWidget(new TextWidget(Coordinate.fromCenter(105, -57), Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.momentum"))   .noShadow()).mainColor(new Color(0, 255, 255)).addAnimation(new Animation.FadeIn.FromRight(0.5), .12);
        addWidget(new TextWidget(Coordinate.fromCenter(152, -57), Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.pressure"))   .noShadow()).mainColor(new Color(0, 255, 0  )).addAnimation(new Animation.FadeIn.FromRight(0.5), .17);

        addWidget(new TextWidget(Coordinate.fromCenter(11  + 45, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(255, 255, 0)).addAnimation(new Animation.FadeIn.FromRight(0.5), .04);
        addWidget(new TextWidget(Coordinate.fromCenter(58  + 45, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(255,   0, 0)).addAnimation(new Animation.FadeIn.FromRight(0.5), .09);
        addWidget(new TextWidget(Coordinate.fromCenter(105 + 45, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(0, 255, 255)).addAnimation(new Animation.FadeIn.FromRight(0.5), .14);
        addWidget(new TextWidget(Coordinate.fromCenter(152 + 46, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(0, 255, 0  )).addAnimation(new Animation.FadeIn.FromRight(0.5), .19);

        addWidget(new RectangleWidget(Coordinate.fromCenter(10, -47), Coordinate.fromTopLeft(188, 12)).mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .05);

        addWidget(new TextWidget(Coordinate.fromCenter(14 , -44), Component.translatable("gui.programmable_magic.machine.wind_turbine.section.current_mana")).noShadow()).addAnimation(new Animation.FadeIn.FromRight(0.5), .1);
        addWidget(new TextWidget(Coordinate.fromCenter(90 , -44), Component.translatable("gui.programmable_magic.machine.wind_turbine.section.max_cache")   ).noShadow()).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);
        addWidget(new TextWidget(Coordinate.fromCenter(147, -44), Component.translatable("gui.programmable_magic.machine.wind_turbine.section.net_power")   ).noShadow()).addAnimation(new Animation.FadeIn.FromRight(0.5), .2);

        // RADIATION
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, -20), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, -20), Coordinate.fromTopLeft(131, 6), menu.radiationStorageJ, menu.radiationCacheJ).bottomAlignY().mainColor(new Color(255, 255, 0))).addAnimation(new Animation.FadeIn.FromRight(0.5), .03);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, -22), menu.radiationStorageJ, 7, 1, true).bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .06);
        addWidget(new TextWidget(Coordinate.fromCenter(56, -21), Component.literal("J")).noShadow().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .09);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, -22), menu.radiationCacheJ, 7, 1, true).bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .12);
        addWidget(new TextWidget(Coordinate.fromCenter(138, -21), Component.literal("J")).noShadow().bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);
        addWidget(new TextWidget(Coordinate.fromCenter(73, -21), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .18);

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -20), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .21);
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -27), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(255, 255, 0))).addAnimation(new Animation.FadeIn.FromRight(0.5), .24);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, -22), menu.radiationPowerW, 7, 1, true).rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .27);
        addWidget(new TextWidget(Coordinate.fromCenter(195, -21), Component.literal("W")).noShadow().rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .3);

        // TEMPERATURE
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, -4), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .03);
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, -4), Coordinate.fromTopLeft(131, 6), menu.temperatureStorageJ, menu.temperatureCacheJ).bottomAlignY().mainColor(new Color(255, 0, 0))).addAnimation(new Animation.FadeIn.FromRight(0.5), .06);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, -6), menu.temperatureStorageJ, 7, 1, true).bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .09);
        addWidget(new TextWidget(Coordinate.fromCenter(56, -5), Component.literal("J")).noShadow().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .12);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, -6), menu.temperatureCacheJ, 7, 1, true).bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);
        addWidget(new TextWidget(Coordinate.fromCenter(138, -5), Component.literal("J")).noShadow().bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .18);
        addWidget(new TextWidget(Coordinate.fromCenter(73, -5), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .21);

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -4), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .24);
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -11), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(255, 0, 0))).addAnimation(new Animation.FadeIn.FromRight(0.5), .27);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, -6), menu.temperaturePowerW, 7, 1, true).rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .30);
        addWidget(new TextWidget(Coordinate.fromCenter(195, -5), Component.literal("W")).noShadow().rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .33);

        // MOMENTUM
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, 12), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .06);
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, 12), Coordinate.fromTopLeft(131, 6), menu.momentumStorageJ, menu.momentumCacheJ).bottomAlignY().mainColor(new Color(0, 255, 255))).addAnimation(new Animation.FadeIn.FromRight(0.5), .09);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, 10), menu.momentumStorageJ, 7, 1, true).bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .12);
        addWidget(new TextWidget(Coordinate.fromCenter(56, 11), Component.literal("J")).noShadow().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, 10), menu.momentumCacheJ, 7, 1, true).bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .18);
        addWidget(new TextWidget(Coordinate.fromCenter(138, 11), Component.literal("J")).noShadow().bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .21);
        addWidget(new TextWidget(Coordinate.fromCenter(73, 11), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .24);

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 12), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .27);
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 5), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(0, 255, 255))).addAnimation(new Animation.FadeIn.FromRight(0.5), .30);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, 10), menu.momentumPowerW, 7, 1, true).rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .33);
        addWidget(new TextWidget(Coordinate.fromCenter(195, 11), Component.literal("W")).noShadow().rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .36);

        // PRESSURE
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, 28), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .09);
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, 28), Coordinate.fromTopLeft(131, 6), menu.pressureStorageJ, menu.pressureCacheJ).bottomAlignY().mainColor(new Color(0, 255, 0))).addAnimation(new Animation.FadeIn.FromRight(0.5), .12);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, 26), menu.pressureStorageJ, 7, 1, true).bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);
        addWidget(new TextWidget(Coordinate.fromCenter(56, 27), Component.literal("J")).noShadow().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .21);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, 26), menu.pressureCacheJ, 7, 1, true).bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .24);
        addWidget(new TextWidget(Coordinate.fromCenter(138, 27), Component.literal("J")).noShadow().bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), .27);
        addWidget(new TextWidget(Coordinate.fromCenter(73, 27), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .30);

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 28), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127))).addAnimation(new Animation.FadeIn.FromRight(0.5), .33);
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 21), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(0, 255, 0))).addAnimation(new Animation.FadeIn.FromRight(0.5), .36);
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, 26), menu.pressurePowerW, 7, 1, true).rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .39);
        addWidget(new TextWidget(Coordinate.fromCenter(195, 27), Component.literal("W")).noShadow().rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), .42);

        // =================== 标题装饰 =================== //
        addWidget(new TextWidget(Coordinate.fromCenter(-198, 40), Component.translatable("gui.programmable_magic.machine.wind_turbine.title.machine_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);
        addWidget(new TextWidget(Coordinate.fromCenter(10, 40), Component.translatable("gui.programmable_magic.machine.wind_turbine.title.network_info")).scaled(2)).addAnimation(new Animation.FadeIn.FromRight(0.5), .00);

    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (synced_enabled) return;
        if (interacted) return;
        boolean enabled = menu.enabled.get();
        if (enabled == initial_enabled) return;
        synced_enabled = true;
        powerSwitch.setPressed(enabled);
        powerSwitch.rectDx.set(enabled ? (double) powerSwitch.w() / 2 : 0);
    }
}
