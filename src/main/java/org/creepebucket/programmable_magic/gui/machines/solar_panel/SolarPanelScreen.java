package org.creepebucket.programmable_magic.gui.machines.solar_panel;

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

public class SolarPanelScreen extends Screen<SolarPanelMenu> {
    public MachineWidgets.TextSwitchWidget powerUnit;
    public SwitchWidget powerSwitch;
    private boolean synced_enabled;
    private boolean initial_enabled;
    private boolean interacted;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

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
        powerCoeffWidget.addDetailLine(Component.literal("常数"), new SyncedValue.StaticDouble(100d), Component.literal("常数"), "+");

        // =================== 功率显示 =================== //

        // 装饰
        addWidget(new RectangleWidget(Coordinate.fromCenter(-198, -58), Coordinate.fromTopLeft(28, 20)).mainColor(new Color(255, 255, 0, 127))).addAnimation(new Animation.FadeIn.FromLeft(0.5), .22);
        addWidget(new TextWidget(Coordinate.fromCenter(-195, -55), Component.literal("P=")).scaled(2).noShadow()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .2);

        addWidget(new RectangleWidget(Coordinate.fromCenter(-10, -58), Coordinate.fromTopLeft(156, 20)).mainColor(new Color(0, 0, 0, 127)).rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .15);
        addWidget(new TextWidget(Coordinate.fromCenter(-11, -55), Component.literal("辐射")).scaled(2).noShadow().rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .1);
        addWidget(new TextWidget(Coordinate.fromCenter(-163, -39), Component.translatable("gui.programmable_magic.machine.wind_turbine.section.type")).noShadow().bottomAlignY().mainColor(new Color(127, 127, 127))).addAnimation(new Animation.FadeIn.FromLeft(0.5), .05);

        // 功率单位
        var mainPowerUnit = new MachineWidgets.TextSwitchWidget(Coordinate.fromCenter(-10, 8), Coordinate.fromTopLeft(28, 18), 2, "W");
        addWidget(mainPowerUnit.rightAlign().mainColor(new Color(-1)).bgColor(new Color(255, 255, 255, 127))).addAnimation(new Animation.FadeIn.FromLeft(0.5), .1);
        addWidget(new RectangleWidget(Coordinate.fromCenter(-10, 26), Coordinate.fromTopLeft(28, 2)).mainColor(new Color(-1)).rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .12);

        // 主功率显示
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(-10, 0), menu.power, 6, 4, mainPowerUnit, "W", false).mainColor(new Color(255, 255, 0)).rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromLeft(0.5), 0);
        for (int i = 0; i < 6; i++) addWidget(new RectangleWidget(Coordinate.fromCenter(-i * 32 - 10, 0), Coordinate.fromTopLeft(28, 2)).mainColor(new Color(255, 255, 0)).rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .02 * i);

        // 开关
        powerSwitch = (SwitchWidget) addWidget(new SwitchWidget(Coordinate.fromCenter(-198, 8), Coordinate.fromTopLeft(60, 20), CommonComponents.OPTION_OFF, CommonComponents.OPTION_ON)
                .setPressed(menu.enabled.get()).onSwitch(enabled -> { interacted = true; SolarPanelHooks.onSwitch(menu, enabled);}).addAnimation(new Animation.FadeIn.FromLeft(0.5), .2));

        // 维护成本
        addWidget(new RectangleWidget(Coordinate.fromCenter(-131, 8), Coordinate.fromTopLeft(44, 20)).mainColor(new Color(0, 0, 0, 127)));
        addWidget(new TextWidget(Coordinate.fromCenter(-129, 10), Component.literal("维护成本::")).noShadow());
        addWidget(new TextWidget(Coordinate.fromCenter(-129, 19), Component.literal("0")) .noShadow().mainColor(new Color(255, 255, 0)));
        addWidget(new TextWidget(Coordinate.fromCenter(-119, 19), Component.literal("0")) .noShadow().mainColor(new Color(255,   0, 0)));
        addWidget(new TextWidget(Coordinate.fromCenter(-109, 19), Component.literal("10")).noShadow().mainColor(new Color(0, 255, 255)));
        addWidget(new TextWidget(Coordinate.fromCenter(-94 , 19), Component.literal("0")) .noShadow().mainColor(new Color(0, 255, 0  )));


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
