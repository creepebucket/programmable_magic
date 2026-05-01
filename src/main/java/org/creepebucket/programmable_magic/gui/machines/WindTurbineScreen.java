package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.MouseCursorWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;

public class WindTurbineScreen extends Screen<WindTurbineMenu> {
    public MachineWidgets.TextSwitchWidget powerUnit;
    int count;

    public WindTurbineScreen(WindTurbineMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        // 计算组件
        addWidget(new TextWidget(Coordinate.fromCenter(-3, -33), Component.literal("x")));

        // 空气密度
        // 扫风面积
        // 风速
        var airSpeedDetailWidget = (MachineWidgets.CalcationDetailsWidget) addWidget(new MachineWidgets.CalcationDetailsWidget(
                Coordinate.fromCenter(5, -35), Coordinate.fromTopLeft(100, 11), menu.number, Component.literal("kg/m^3"), Component.literal("hello world")));
        airSpeedDetailWidget.addDetailLine(Component.literal("genshin"), menu.number);
        airSpeedDetailWidget.addDetailLine(Component.literal("impact"), menu.number);
        airSpeedDetailWidget.addDetailLine(Component.literal("honkai"), menu.number);
        airSpeedDetailWidget.addDetailLine(Component.literal("starrail"), menu.number);
        // 功率系数

        // 功率计
        powerUnit = (MachineWidgets.TextSwitchWidget) addWidget(new MachineWidgets.TextSwitchWidget(Coordinate.fromCenter(121, -22), Coordinate.fromTopRight(0,27), 3, "W"));
        addWidget(new MachineWidgets.NumberDisplayWidget(Coordinate.fromCenter(-118, -22), menu.number, 6, 5, powerUnit, "W", false));

        addWidget(new RectangleWidget(Coordinate.fromCenterLeft(0, -22), Coordinate.fromCenterTop(-122, 27)).mainColor(new Color(0x80000000)));
        addWidget(new TextWidget(Coordinate.fromCenter(-140, -22), Component.literal("=")).scaled(3).noShadow());

        addWidget(new MouseCursorWidget());
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (count == 15) {
            menu.number.set((double) ModUtils.simpleRandInt(0, 9999));
            count = 0;
        }
        count++;
    }
}
