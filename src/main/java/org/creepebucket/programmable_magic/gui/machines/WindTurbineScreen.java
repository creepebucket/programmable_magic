package org.creepebucket.programmable_magic.gui.machines;

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
        addWidget(new RectangleWidget(Coordinate.fromCenter(-198, -60), Coordinate.fromTopLeft(23, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(-196, -60), Component.literal("0.5x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY());

        // 空气密度
        var airDensityWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-173, -60), Coordinate.fromTopLeft(79, 11), menu.airDensity,
                Component.literal("kg/m^3"), Component.translatable("gui.programmable_magic.machine.wind_turbine.air_density")).mainColor(new Color(0, 255, 255)).bottomAlignY());

        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.humidity_factor"), menu.airDensityHumidFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.humidity_factor"), "x");
        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.temperature_factor"), menu.airDensityTempFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.temperature_factor"), "x");
        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.pressure_factor"), menu.airDensityPressureFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.pressure_factor"), "x");
        airDensityWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_density"), menu.airDensityBase, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.air_density.base_density"), "+");

        // x
        addWidget(new RectangleWidget(Coordinate.fromCenter(-92, -60), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(-89, -60), Component.literal("x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY());

        // 扫风面积
        var sweptAreaWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(-80, -60), Coordinate.fromTopLeft(78, 11), new SyncedValue.StaticDouble(6.0),
                Component.literal("m^2"), Component.translatable("gui.programmable_magic.machine.wind_turbine.swept_area")).mainColor(new Color(0, 255, 255)).bottomAlignY());

        sweptAreaWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_area"), new SyncedValue.StaticDouble(6.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.swept_area.base_area"), "+");

        // x
        addWidget(new RectangleWidget(Coordinate.fromCenter(1, -60), Coordinate.fromTopLeft(11, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(4, -60), Component.literal("x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY());

        // 风速
        var airSpeedDetailWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(14, -60), Coordinate.fromTopLeft(79, 11), menu.windSpeed, Component.literal("m/s"),
                Component.translatable("gui.programmable_magic.machine.wind_turbine.wind_speed")).mainColor(new Color(0, 255, 255)).bottomAlignY());

        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.weather_factor"), menu.windSpeedWeatherFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.weather_factor"), "x");
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.time_factor"), menu.windSpeedTimeFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.time_factor"), "x");
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.altitude_factor"), menu.windSpeedAltitudeFact, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.altitude_factor"), "x");
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.wind_shear_exponent"), menu.windShearExponent, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.wind_shear_exponent"), " ");
        airSpeedDetailWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_speed"), menu.windSpeedBase, Component.translatable("tooltip.programmable_magic.machine.wind_turbine.wind_speed.base_speed"), "+");

        // ^3x
        addWidget(new RectangleWidget(Coordinate.fromCenter(95, -60), Coordinate.fromTopLeft(23, 11)).mainColor(new Color(0, 0, 0, 127)).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(97, -60), Component.literal("^3x")).noShadow().mainColor(new Color(127, 127, 127)).bottomAlignY());

        // 功率系数
        var powerCoeffWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(Coordinate.fromCenter(120, -60), Coordinate.fromTopLeft(78, 11), new SyncedValue.StaticDouble(25.0),
                Component.literal("%"), Component.translatable("gui.programmable_magic.machine.wind_turbine.power_coefficient")).mainColor(new Color(0, 255, 255)).bottomAlignY());

        powerCoeffWidget.addDetailLine(Component.translatable("gui.programmable_magic.machine.wind_turbine.detail.base_coefficient"), new SyncedValue.StaticDouble(25.0), Component.translatable("tooltip.programmable_magic.machine.wind_turbine.power_coefficient.base_coefficient"), "+");

        // =================== 功率显示 =================== //

        // 装饰
        addWidget(new RectangleWidget(Coordinate.fromCenter(-198, -58), Coordinate.fromTopLeft(28, 20)).mainColor(new Color(0, 255, 255, 127)));
        addWidget(new TextWidget(Coordinate.fromCenter(-195, -55), Component.literal("P=")).scaled(2).noShadow());

        addWidget(new RectangleWidget(Coordinate.fromCenter(-10, -58), Coordinate.fromTopLeft(156, 20)).mainColor(new Color(0, 0, 0, 127)).rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(-11, -55), Component.literal("Momentum")).scaled(2).noShadow().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(-163, -39), Component.literal("TYPE::")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)));

        // 功率单位
        var mainPowerUnit = new MachineWidgets.TextSwitchWidget(Coordinate.fromCenter(-10, 8), Coordinate.fromTopLeft(28, 18), 2, "W");
        addWidget(mainPowerUnit.rightAlign().mainColor(new Color(-1)).bgColor(new Color(255, 255, 255, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(-10, 26), Coordinate.fromTopLeft(28, 2)).mainColor(new Color(-1)).rightAlign());

        // 主功率显示
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(-10, 0), menu.power, 6, 4, mainPowerUnit, "W", false).mainColor(new Color(0, 255, 255)).rightAlign().bottomAlignY());
        for (int i = 0; i < 6; i++) addWidget(new RectangleWidget(Coordinate.fromCenter(-i * 32 - 10, 0), Coordinate.fromTopLeft(28, 2)).mainColor(new Color(0, 255, 255)).rightAlign());

        // 开关
        powerSwitch = (SwitchWidget) addWidget(new SwitchWidget(Coordinate.fromCenter(-198, 8), Coordinate.fromTopLeft(60, 20), Component.literal("OFF"), Component.literal("ON"))
                .setPressed(menu.enabled.get()).onSwitch(enabled -> { interacted = true; WindTurbineHooks.onSwitch(menu, enabled);}).addAnimation(new Animation.FadeIn.FromLeft(0.5), 0.1));

        initial_enabled = menu.enabled.get();
        synced_enabled = false;
        interacted = false;

        // =================== 网络状态 =================== //

        //.mainColor(new Color(255, 255, 0))
        //.mainColor(new Color(255,   0, 0))
        //.mainColor(new Color(0, 255, 255))
        //.mainColor(new Color(0, 255, 0  ))

        addWidget(new RectangleWidget(Coordinate.fromCenter(10 , -58), Coordinate.fromTopLeft(46, 9)).mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(57 , -58), Coordinate.fromTopLeft(46, 9)).mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(104, -58), Coordinate.fromTopLeft(46, 9)).mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(151, -58), Coordinate.fromTopLeft(47, 9)).mainColor(new Color(0, 0, 0, 127)));

        addWidget(new TextWidget(Coordinate.fromCenter(11 , -57), Component.literal("Radi")).noShadow()).mainColor(new Color(255, 255, 0));
        addWidget(new TextWidget(Coordinate.fromCenter(58 , -57), Component.literal("Temp")).noShadow()).mainColor(new Color(255,   0, 0));
        addWidget(new TextWidget(Coordinate.fromCenter(105, -57), Component.literal("Mome")).noShadow()).mainColor(new Color(0, 255, 255));
        addWidget(new TextWidget(Coordinate.fromCenter(152, -57), Component.literal("Pres")).noShadow()).mainColor(new Color(0, 255, 0  ));

        addWidget(new TextWidget(Coordinate.fromCenter(11  + 45, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(255, 255, 0));
        addWidget(new TextWidget(Coordinate.fromCenter(58  + 45, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(255,   0, 0));
        addWidget(new TextWidget(Coordinate.fromCenter(105 + 45, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(0, 255, 255));
        addWidget(new TextWidget(Coordinate.fromCenter(152 + 46, -57), Component.literal("::")).noShadow()).rightAlign().mainColor(new Color(0, 255, 0  ));

        addWidget(new RectangleWidget(Coordinate.fromCenter(10, -47), Coordinate.fromTopLeft(188, 12)).mainColor(new Color(0, 0, 0, 127)));

        addWidget(new TextWidget(Coordinate.fromCenter(14 , -44), Component.literal("Current Mana")).noShadow());
        addWidget(new TextWidget(Coordinate.fromCenter(90 , -44), Component.literal("Max Cache")   ).noShadow());
        addWidget(new TextWidget(Coordinate.fromCenter(147, -44), Component.literal("Net Power")   ).noShadow());

        // RADIATION
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, -20), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, -20), Coordinate.fromTopLeft(131, 6), menu.radiationStorageJ, menu.radiationCacheJ).bottomAlignY().mainColor(new Color(255, 255, 0)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, -22), menu.radiationStorageJ, 7, 1, true).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(56, -21), Component.literal("J")).noShadow().bottomAlignY());
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, -22), menu.radiationCacheJ, 7, 1, true).bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(138, -21), Component.literal("J")).noShadow().bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(73, -21), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)));

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -20), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -27), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(255, 255, 0)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, -22), menu.radiationPowerW, 7, 1, true).rightAlign().bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(195, -21), Component.literal("W")).noShadow().rightAlign().bottomAlignY());

        // TEMPERATURE
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, -4), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, -4), Coordinate.fromTopLeft(131, 6), menu.temperatureStorageJ, menu.temperatureCacheJ).bottomAlignY().mainColor(new Color(255, 0, 0)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, -6), menu.temperatureStorageJ, 7, 1, true).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(56, -5), Component.literal("J")).noShadow().bottomAlignY());
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, -6), menu.temperatureCacheJ, 7, 1, true).bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(138, -5), Component.literal("J")).noShadow().bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(73, -5), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)));

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -4), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, -11), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(255, 0, 0)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, -6), menu.temperaturePowerW, 7, 1, true).rightAlign().bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(195, -5), Component.literal("W")).noShadow().rightAlign().bottomAlignY());

        // MOMENTUM
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, 12), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, 12), Coordinate.fromTopLeft(131, 6), menu.momentumStorageJ, menu.momentumCacheJ).bottomAlignY().mainColor(new Color(0, 255, 255)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, 10), menu.momentumStorageJ, 7, 1, true).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(56, 11), Component.literal("J")).noShadow().bottomAlignY());
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, 10), menu.momentumCacheJ, 7, 1, true).bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(138, 11), Component.literal("J")).noShadow().bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(73, 11), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)));

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 12), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 5), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(0, 255, 255)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, 10), menu.momentumPowerW, 7, 1, true).rightAlign().bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(195, 11), Component.literal("W")).noShadow().rightAlign().bottomAlignY());

        // PRESSURE
        addWidget(new RectangleWidget(Coordinate.fromCenter(10, 28), Coordinate.fromTopLeft(131, 13)).bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new ProgressBarWidget(Coordinate.fromCenter(10, 28), Coordinate.fromTopLeft(131, 6), menu.pressureStorageJ, menu.pressureCacheJ).bottomAlignY().mainColor(new Color(0, 255, 0)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(13, 26), menu.pressureStorageJ, 7, 1, true).bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(56, 27), Component.literal("J")).noShadow().bottomAlignY());
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(137, 26), menu.pressureCacheJ, 7, 1, true).bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(138, 27), Component.literal("J")).noShadow().bottomAlignY().rightAlign());
        addWidget(new TextWidget(Coordinate.fromCenter(73, 27), Component.literal("/")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)));

        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 28), Coordinate.fromTopLeft(54, 13)).rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127)));
        addWidget(new RectangleWidget(Coordinate.fromCenter(198, 21), Coordinate.fromTopLeft(54, 6)).rightAlign().bottomAlignY().mainColor(new Color(0, 255, 0)));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(194, 26), menu.pressurePowerW, 7, 1, true).rightAlign().bottomAlignY());
        addWidget(new TextWidget(Coordinate.fromCenter(195, 27), Component.literal("W")).noShadow().rightAlign().bottomAlignY());
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
