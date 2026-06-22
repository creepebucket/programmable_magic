package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.*;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;

import java.util.ArrayList;
import java.util.List;

public class MachineWidgets {

    public static class CalcationDetailsWidget extends Widget implements Renderable, Clickable, Lifecycle {
        public DynamicValue<Double> result;
        public Component resultUnit, description;
        public Widget descWidget;
        public boolean isExpanded = false;
        public List<DetailLineWidget> detailLines = new ArrayList<>();

        public CalcationDetailsWidget(Coordinate pos, Coordinate size, DynamicValue<Double> result, Component resultUnit, Component description) {
            super(pos, size);

            this.result = result;
            this.resultUnit = resultUnit;
            this.description = description;
        }

        @Override
        public void onInitialize() {

            // 计算结果
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(1, 1), result, 6, 1, true));
            // 计算结果单位
            addChild(new TextWidget(Coordinate.fromTopRight(-1, 2), resultUnit).noShadow().rightAlign().mainColor(textColor()));
            // 描述
            descWidget = addChild(new RectangleWidget(Coordinate.fromTopLeft(0, -1), Coordinate.fromTopRight(0, 4)).bottomAlignY());
            descWidget.addChild(new TextWidget(Coordinate.fromTopLeft(2, 4), description).bottomAlignY().mainColor(textColor()));
        }

        @Override
        public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
            isExpanded = !isExpanded;
            int c = 0;

            if (isExpanded) {
                for(DetailLineWidget i :detailLines.reversed()) {
                    i.addAnimation(new Animation.FadeIn.FromBottom(0.2), c * 0.05 + 0.2);
                    c++;
                }
            } else {
                for(DetailLineWidget i :detailLines.reversed()) {
                    i.addAnimation(new Animation.FadeOut.ToBottom(0.2).noDeletion(), c * 0.05);
                    c++;
                }
            }

            return true;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(left(), top(), right(), bottom(), bgColorInt());

            descWidget.dy.set(isExpanded ? -10 * detailLines.size() : 0);
        }

        public void addDetailLine(Component desc, DynamicValue<Double> number, Component tooltip, String operation) {
            var line = addChild(new DetailLineWidget(Coordinate.fromTopLeft(0, (detailLines.size() + 1) * -10), originalSize, desc, number, bgColor(), mainColor(), operation));
            line.disable().tooltip(tooltip);
            detailLines.add((DetailLineWidget) line);
        }

        public static class DetailLineWidget extends Widget {
            public DetailLineWidget(Coordinate pos, Coordinate size, Component desc, DynamicValue<Double> number, Color bgColor, Color mainColor, String operation) {
                super(pos, size);
                addChild(new RectangleWidget(Coordinate.ZERO, Coordinate.fromTopLeft(9, 9)).mainColor(bgColor));
                addChild(new TextWidget(Coordinate.fromTopLeft(2, 1), Component.literal(operation)).noShadow().mainColor(new Color(127, 127, 127)));
                addChild(new RectangleWidget(Coordinate.fromTopLeft(11, 0), size.add(Coordinate.fromTopLeft(-11, -2))).mainColor(bgColor));
                addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(11, 0), number, 6, 1, true).mainColor(new Color(-1)));
                addChild(new TextWidget(Coordinate.fromTopLeft(size.toScreenX() - 1, size.toScreenY() - 1), desc).scaled(0.5).noShadow().rightAlign().bottomAlignY().mainColor(new Color(127, 127, 127)));
            }
        }
    }

    public static class NetworkInfoWidget extends Widget implements Lifecycle {
        public MachineMenu menu;

        public NetworkInfoWidget(Coordinate pos, MachineMenu menu) {
            super(pos, Coordinate.fromTopLeft(0, 0));
            this.menu = menu;
        }

        @Override
        public void onInitialize() {
            // 顶部标头
            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopLeft(188, 14))
                    .mainColor(new Color(0, 0, 0, 127)))
                    .addAnimation(new Animation.FadeIn.FromRight(0.5), .05);

            addChild(new TextWidget(Coordinate.fromTopLeft(4, 3),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.section.current_mana"))
                    .noShadow()).addAnimation(new Animation.FadeIn.FromRight(0.5), .1);
            addChild(new TextWidget(Coordinate.fromTopLeft(80, 3),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.section.max_cache"))
                    .noShadow()).addAnimation(new Animation.FadeIn.FromRight(0.5), .15);
            addChild(new TextWidget(Coordinate.fromTopLeft(137, 3),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.section.net_power"))
                    .noShadow()).addAnimation(new Animation.FadeIn.FromRight(0.5), .2);

            // 四个魔力行
            addManaRow(-7, menu.radiationStorageJ, menu.radiationCacheJ, menu.radiationPowerW,
                    new Color(255, 255, 0),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.radiation"));
            addManaRow(11, menu.temperatureStorageJ, menu.temperatureCacheJ, menu.temperaturePowerW,
                    new Color(255, 0, 0),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.temperature"));
            addManaRow(29, menu.momentumStorageJ, menu.momentumCacheJ, menu.momentumPowerW,
                    new Color(0, 255, 255),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.momentum"));
            addManaRow(47, menu.pressureStorageJ, menu.pressureCacheJ, menu.pressurePowerW,
                    new Color(0, 255, 0),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.mana.pressure"));
        }

        private void addManaRow(int y, DynamicValue<Double> storageJ, DynamicValue<Double> cacheJ,
                                DynamicValue<Double> powerW, Color mainColor, Component label) {
            int baseY = y + 38;
            double[] delays = {.00, .03, .06, .09, .12, .15, .18, .21, .24, .27, .30};

            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, baseY+1), Coordinate.fromTopLeft(131, 15))
                    .bottomAlignY().mainColor(new Color(0, 0, 0, 127)))
                    .addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[0]);
            addChild(new ProgressBarWidget(Coordinate.fromTopLeft(0, baseY + 1), Coordinate.fromTopLeft(131, 6), storageJ, cacheJ)
                    .bottomAlignY().mainColor(mainColor))
                    .addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[1]);
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(3, baseY - 2), storageJ, 7, 1, true)
                    .bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[2]);
            addChild(new TextWidget(Coordinate.fromTopLeft(46, baseY-1), Component.literal("J"))
                    .noShadow().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[3]);
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(127, baseY - 2), cacheJ, 7, 1, true)
                    .bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[4]);
            addChild(new TextWidget(Coordinate.fromTopLeft(128, baseY-1), Component.literal("J"))
                    .noShadow().bottomAlignY().rightAlign()).addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[5]);
            addChild(new TextWidget(Coordinate.fromTopLeft(63, baseY-1), Component.literal("/"))
                    .noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)))
                    .addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[6]);

            addChild(new RectangleWidget(Coordinate.fromTopLeft(188, baseY+1), Coordinate.fromTopLeft(54, 15))
                    .rightAlign().bottomAlignY().mainColor(new Color(0, 0, 0, 127)))
                    .addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[7]);
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(184, baseY - 2), powerW, 7, 1, true)
                    .rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[9]).mainColor(mainColor);
            addChild(new TextWidget(Coordinate.fromTopLeft(185, baseY-1), Component.literal("W"))
                    .noShadow().rightAlign().bottomAlignY()).addAnimation(new Animation.FadeIn.FromRight(0.5), y * 0.001 + delays[10]).mainColor(mainColor);
        }
    }

    public static class PowerDisplayWidget extends Widget implements Lifecycle, Tickable {
        public DynamicValue<Double> power;
        public MachineMenu menu;
        public Color mainColor;
        public Component sectionLabel;
        public TextSwitchWidget unit;
        public SwitchWidget powerSwitch;
        public java.util.function.Consumer<Boolean> onSwitchCallback;
        public boolean synced_enabled, initial_enabled, interacted;

        public PowerDisplayWidget(Coordinate pos, MachineMenu menu, DynamicValue<Double> power,
                                  Color mainColor, Component sectionLabel,
                                  java.util.function.Consumer<Boolean> onSwitchCallback) {
            super(pos, Coordinate.fromTopLeft(0, 0));
            this.menu = menu;
            this.power = power;
            this.mainColor = mainColor;
            this.sectionLabel = sectionLabel;
            this.onSwitchCallback = onSwitchCallback;
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopLeft(28, 20))
                    .mainColor(new Color(mainColor.r, mainColor.g, mainColor.b, 127)))
                    .addAnimation(new Animation.FadeIn.FromLeft(0.5), .22);
            addChild(new TextWidget(Coordinate.fromTopLeft(3, 3), Component.literal("P="))
                    .scaled(2).noShadow()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .2);

            addChild(new RectangleWidget(Coordinate.fromTopLeft(188, 0), Coordinate.fromTopLeft(156, 20))
                    .mainColor(new Color(0, 0, 0, 127)).rightAlign())
                    .addAnimation(new Animation.FadeIn.FromLeft(0.5), .15);
            addChild(new TextWidget(Coordinate.fromTopLeft(187, 3), sectionLabel)
                    .scaled(2).noShadow().rightAlign()).addAnimation(new Animation.FadeIn.FromLeft(0.5), .1);
            addChild(new TextWidget(Coordinate.fromTopLeft(35, 19),
                    Component.translatable("gui.programmable_magic.machine.wind_turbine.section.type"))
                    .noShadow().bottomAlignY().mainColor(new Color(127, 127, 127)))
                    .addAnimation(new Animation.FadeIn.FromLeft(0.5), .05);

            unit = new TextSwitchWidget(Coordinate.fromTopLeft(188, 66), Coordinate.fromTopLeft(28, 18), 2, "W");
            addChild(unit.rightAlign().mainColor(new Color(-1)).bgColor(new Color(255, 255, 255, 127)))
                    .addAnimation(new Animation.FadeIn.FromLeft(0.5), .1);
            addChild(new RectangleWidget(Coordinate.fromTopLeft(188, 84), Coordinate.fromTopLeft(28, 2))
                    .mainColor(new Color(-1)).rightAlign())
                    .addAnimation(new Animation.FadeIn.FromLeft(0.5), .12);

            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(188, 58), power, 6, 4, unit, "W", false)
                    .mainColor(mainColor).rightAlign().bottomAlignY())
                    .addAnimation(new Animation.FadeIn.FromLeft(0.5), 0);
            for (int i = 0; i < 6; i++)
                addChild(new RectangleWidget(Coordinate.fromTopLeft(188 - i * 32, 58),
                        Coordinate.fromTopLeft(28, 2)).mainColor(mainColor).rightAlign())
                        .addAnimation(new Animation.FadeIn.FromLeft(0.5), .02 * i);

            powerSwitch = (SwitchWidget) addChild(new SwitchWidget(
                    Coordinate.fromTopLeft(0, 66), Coordinate.fromTopLeft(60, 20),
                    net.minecraft.network.chat.CommonComponents.OPTION_OFF,
                    net.minecraft.network.chat.CommonComponents.OPTION_ON)
                    .setPressed(menu.enabled.get())
                    .onSwitch(enabled -> {
                        interacted = true;
                        onSwitchCallback.accept(enabled);
                    }).addAnimation(new Animation.FadeIn.FromLeft(0.5), .2));

            initial_enabled = menu.enabled.get();
            synced_enabled = false;
            interacted = false;
        }

        @Override
        public void tick() {
            if (synced_enabled) return;
            if (interacted) return;
            boolean enabled = menu.enabled.get();
            if (enabled == initial_enabled) return;
            synced_enabled = true;
            powerSwitch.setPressed(enabled);
            powerSwitch.rectDx.set(enabled ? (double) powerSwitch.w() / 2 : 0);
        }
    }

    public static class WindowHintWidget extends Widget implements Lifecycle, Tickable, KeyInputable {
        public Widget hintText;

        public WindowHintWidget(Coordinate pos) {
            super(pos, Coordinate.ZERO);
        }

        @Override
        public void onInitialize() {
            hintText = screen.addWidget(new TextWidget(Coordinate.fromCenter(0, 0), Component.literal("按下 Alt+W 开关窗口管理界面")).centerAlign().centerAlignY().mainColor(-1).disable());
        }

        @Override
        public void tick() {
            if (((MachineScreen<?>) screen).windows.stream().noneMatch((w) -> w.enabled)) {
                if (!hintText.enabled) {
                    hintText.enable();
                    hintText.addAnimation(new Animation.FadeIn.FromTop(0.3), 0);
                }
            } else {
                if (hintText.enabled || hintText.animations.isEmpty()) {
                    hintText.addAnimation(new Animation.FadeOut.ToBottom(0.3).noDeletion(), 0);
                }
            }
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.hasAltDown() && event.key() == 'W') {
                var w = ((MachineScreen) screen).managementWindow;
                if (w.enabled) {
                    w.addAnimation(new Animation.FadeOut.ToBottom(0.3).noDeletion(), 0);
                } else {
                    w.addAnimation(new Animation.FadeIn.FromTop(0.3), 0);
                    w.enable();
                }
            }

            return false;
        }
    }

    public static class InformationWindowWidget extends Widget implements Lifecycle, MouseDraggable, Clickable, Renderable {
        public Component name;
        public boolean changingPosition, changingSize;
        public Widget closeButton;
        public int minW, minH;

        public InformationWindowWidget(Coordinate pos, Coordinate size, Component name, int minW, int minH) {
            super(pos, size);

            this.name = name;
            this.minW = minW;
            this.minH = minH;
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            if (changingPosition) {
                var targetX = dx.target + dragX;
                var targetY = dy.target + dragY;

                // 判断会不会超出屏幕
                targetX = Math.clamp(targetX, -originalX(), getScreenW() - originalX() - w());
                targetY = Math.clamp(targetY, -originalY(), getScreenH() - originalY() - h());

                dx.set(targetX);
                dy.set(targetY);
                return true;
            }
            else if (changingSize) {
                var targetX = dw.target + dragX;
                var targetY = dh.target + dragY;

                // 判断会不会超出屏幕
                targetX = Math.max(targetX, minW - originalW());
                targetY = Math.max(targetY, minH - originalH());

                dw.set(targetX);
                dh.set(targetY);

                return true;
            }
            return false;
        }

        @Override
        public void onInitialize() {
            dx.a = 250;
            dy.a = 250;
            dw.a = 250;
            dh.a = 250;

            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopRight(0, 11)).mainColor(0xbf000000));
            var a = addChild(new TextWidget(Coordinate.fromTopLeft(3, 2), Component.literal("// ").append(name)).noShadow().mainColor(0xff7f7f7f));
            addChild(new RectangleWidget(Coordinate.fromTopLeft(a.w() + 6, 5), Coordinate.fromTopRight(-a.w() - 10, 1)).mainColor(0x7f7f7f7f));
            closeButton = addChild(new RectangleWidget(Coordinate.fromTopRight(-7, 4), Coordinate.fromTopLeft(3, 3)));

            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, 11), Coordinate.fromBottomRight(0, -11)).mainColor(0x7f000000));

            addAnimation(new Animation.FadeIn.FromTop(0.3), (double) ModUtils.simpleRandInt(0, 2) / 20);
        }

        @Override
        public Widget enable() {
            if (animations.isEmpty()) addAnimation(new Animation.FadeIn.FromTop(0.3), 0);
            return super.enable();
        }

        @Override
        public Widget disable() {
            if (animations.isEmpty()) addAnimation(new Animation.FadeOut.ToBottom(0.3).noDeletion(), 0);
            else return super.disable();
            return this;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            if (isInBounds(event.x(), event.y())) {
                parent.children.remove(this);
                parent.children.add(this); // 提高绘制优先级
            }

            if (isIn(event.x(), event.y(), right() - 9, top() + 2, 7, 7)) { // 关闭按钮
                addAnimation(new Animation.FadeOut.ToBottom(0.3).noDeletion(), 0);
                return true;
            }
            else if (isIn(event.x(), event.y(), x(), y(), w(), 11)) { // 标题栏
                changingPosition = true;
                return true;
            }
            else if (isIn(event.x(), event.y(), right() - 5, bottom() - 5, 5, 5)) { // 改变尺寸
                changingSize = true;
                return true;
            }
		else if (enabled && isInBounds(event.x(), event.y())) {
				// 手动分发事件
				for (Widget widget : allChild()) {
                    if (widget instanceof Clickable clickable) {
                        if (clickable.mouseClicked(event, fromMouse)) return true;

                        if (widget.isInBounds(event.x(), event.y()) && clickable.mouseClickedChecked(event, fromMouse))
                            return true;
                    }
                    if (!widget.clickBehaviors.isEmpty() && widget.enabled && widget.isInBounds(event.x(), event.y())) {
                        for (Runnable behavior : widget.clickBehaviors) behavior.run();
                        return true;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void onDestroy() {
            ((MachineScreen<?>) screen).windows.remove(this);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            changingPosition = false;
            changingSize = false;
            return false;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            closeButton.originalMainColor = null;
            if (isIn(mouseX, mouseY, right() - 5, bottom() - 5, 5, 5) || changingSize) {
                // 大小改变提示线
                graphics.fill(right() - 4, bottom() + 1, right(), bottom(), -1);
                graphics.fill(right() + 1, bottom() - 4, right(), bottom(), -1);
            }
            else if (isIn(mouseX, mouseY, right() - 9, top() + 2, 7, 7)) {
                closeButton.mainColor(0xffff0000);
            }
        }
    }

    public static class WindowManagementWindow extends InformationWindowWidget implements Tickable{
        public List<SwitchWidget> switches = new ArrayList<>();

        public WindowManagementWindow(Coordinate pos, Coordinate size) {
            super(pos, size, Component.literal("窗口管理"), 120, 60);
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            // 表头
            addChild(new TextWidget(Coordinate.fromTopLeft(7, 15), Component.literal("窗口名称")).noShadow().mainColor(0xffbfbfbf));
            addChild(new TextWidget(Coordinate.fromTopRight(-16, 15), Component.literal("开关")).noShadow().rightAlign().mainColor(0xffbfbfbf));

            // 提示线
            addChild(new RectangleWidget(Coordinate.fromTopLeft(7, 26), Coordinate.fromTopRight(-14, 1)).mainColor(0x7f00bfbf));

            var cull = addChild(new CullAreaWidget(Coordinate.fromTopLeft(7, 28), Coordinate.fromBottomRight(-14, -35)));
            var itemDy = new SmoothedValue(0);
            smoothedValues.add(itemDy);
            var y = 0;

            for (InformationWindowWidget window: ((MachineScreen<?>) screen).windows) {
                cull.addChild(new TextWidget(Coordinate.fromTopLeft(0, 3 + y), window.name).noShadow().mainColor(-1).dy(itemDy));
                switches.add((SwitchWidget) cull.addChild(new SwitchWidget(Coordinate.fromTopRight(-4, 1 + y), Coordinate.fromTopLeft(60, 11), Component.literal("关闭"),
                        Component.literal("开启")).setPressed(window.enabled).onSwitch(b -> {if (b) window.enable(); else window.disable();}).rightAlign().dy(itemDy)));
                y += 15;
            }

            cull.addChild(new ScrollbarWidget(Coordinate.fromTopRight(-3, 1), Coordinate.fromBottomLeft(3, -2), Coordinate.fromTopLeft(-y, 0), itemDy, "y").reverseDirection());
        }

        @Override
        public void tick() {
            var windows = ((MachineScreen<?>) screen).windows;
            for (int i = 0; i < windows.size(); i++) {
                switches.get(i).setPressed(windows.get(i).enabled && windows.get(i).animations.stream().noneMatch(animation -> animation instanceof Animation.FadeOut));
            }
        }
    }

    public static class PowerInfoItemWidget extends Widget implements Lifecycle, Clickable {
        public Component name, unit;
        public DynamicValue<Double> value;

        public PowerInfoItemWidget(Coordinate pos, Component name, DynamicValue<Double> value, Component unit) {
            super(pos, Coordinate.ZERO);
            
            this.value = value;
            this.name = name;
            this.unit = unit;
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopLeft(1, 19)).mainColor(0xff7f7f7f));
            var a = addChild(new TextWidget(Coordinate.fromTopLeft(3, 9), name).noShadow().mainColor(0xffbfbfbf).bottomAlignY());
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(3, 10), value, 6, 1, true));
            var b = addChild(new TextWidget(Coordinate.fromTopLeft(41, 11), unit).noShadow().mainColor(-1));
            
            originalSize.x = (w, h) -> Math.max(a.w(), 41 + b.w());
            originalSize.y = (w, h) -> 18;
        }
    }

    public static class PowerInfoWindow extends InformationWindowWidget {
        public Component powerExpr;
        private int powerInfoItemCount;

        public PowerInfoItemWidget addPowerInfoItem(Component name, DynamicValue<Double> value, Component unit) {
            int index = powerInfoItemCount++;
            int row = index / 2;
            Coordinate pos;
            if (index % 2 == 0)
                pos = Coordinate.fromTopLeft(7, 18 + row * 25);
            else
                pos = Coordinate.fromCenterTop(7, 18 + row * 25);
            var item = new PowerInfoItemWidget(pos, name, value, unit);
            addChild(item);
            return item;
        }

        public PowerInfoWindow(Coordinate pos, Coordinate size, Component powerExpr) {
            super(pos, size, Component.literal("功率计算"), 150, 90);
            this.powerExpr = powerExpr;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new RectangleWidget(Coordinate.fromBottomLeft(7, -7), Coordinate.fromTopRight(-14, 18)).mainColor(0x7f000000).bottomAlignY());
            addChild(new TextWidget(Coordinate.fromCenterBottom(0, -11), powerExpr).noShadow().centerAlign().bottomAlignY().mainColor(0xffbfbfbf));
        }
    }

    public static class MachineInfoWindow extends InformationWindowWidget {
        public DynamicValue<Double> power;
        public Component manaType;
        public Component machineType;

        public MachineInfoWindow(Coordinate pos, Coordinate size, DynamicValue power, Component manaType, Component machineType) {
            super(pos, size, Component.literal("机器总览"), 210, 40);

            this.manaType = manaType;
            this.power = power;
            this.machineType = machineType;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new TextWidget(Coordinate.fromTopLeft(7, 39), Component.literal("机器类型")).noShadow().bottomAlignY().mainColor(0xff7f7f7f));
            addChild(new TextWidget(Coordinate.fromTopLeft(7, 15), machineType).scaled(1.5));
            addChild(new TextWidget(Coordinate.fromTopRight(-7, 39), Component.literal("魔力类型")).noShadow().rightAlign().bottomAlignY().mainColor(0xff7f7f7f));
            addChild(new TextWidget(Coordinate.fromTopRight(-7, 15), manaType).scaled(1.5).rightAlign());
            addChild(new TextWidget(Coordinate.fromCenterBottom(0, -33), Component.literal("[输出功率]")).centerAlign().bottomAlignY().mainColor(0xff7f7f7f));
            addChild(new TextWidget(Coordinate.fromCenterBottom(-72, -5), Component.literal("P=")).noShadow().scaled(2).rightAlign().bottomAlignY().mainColor(0xffffffff));
            var unit = new TextSwitchWidget(Coordinate.fromCenterBottom(73, -5), Coordinate.fromTopLeft(26, 18), 2, "W");
            addChild(unit.bottomAlignY().mainColor(0xffffffff));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterBottom(0, -5), power, 6, 3, unit, "W", false).centerAlign().bottomAlignY());
        }
    }

    public static class NetworkInfoWindow extends InformationWindowWidget {
        public MachineMenu menu;

        public NetworkInfoWindow(Coordinate pos, Coordinate size, MachineMenu menu) {
            super(pos, size, Component.literal("网络信息"), 180, 90);
            this.menu = menu;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            // 提示文本
            addChild(new RectangleWidget(Coordinate.fromTopLeft(7, 14), Coordinate.fromTopLeft(2, 10)).mainColor(0xbfbfbfbf));
            addChild(new TextWidget(Coordinate.fromTopLeft(11, 15), Component.literal("当前魔力")).noShadow().mainColor(0xffbfbfbf));
            addChild(new TextWidget(Coordinate.fromTopRight(-66, 15), Component.literal("魔力缓存")).noShadow().rightAlign().mainColor(0xffbfbfbf));
            addChild(new TextWidget(Coordinate.fromTopRight(-7, 15), Component.literal("净功率")).noShadow().rightAlign().mainColor(0xffbfbfbf));

            // 提示线
            addChild(new RectangleWidget(Coordinate.custom(0, 7, 0   , 29), Coordinate.custom(0, 2, 0.25, -11)).mainColor(0xbfffff00));
            addChild(new RectangleWidget(Coordinate.custom(0, 7, 0.25, 21), Coordinate.custom(0, 2, 0.25, -11)).mainColor(0xbfff0000));
            addChild(new RectangleWidget(Coordinate.custom(0, 7, 0.5 , 13), Coordinate.custom(0, 2, 0.25, -11)).mainColor(0xbf00ffff));
            addChild(new RectangleWidget(Coordinate.custom(0, 7, 0.75, 5 ), Coordinate.custom(0, 2, 0.25, -11)).mainColor(0xbf00ff00));

            // 当前存储
            addChild(new NumberDisplayWidget(Coordinate.custom(0, 11, 0   , 29), menu.radiationStorageJ  , 7, 1, true).mainColor(-1));
            addChild(new NumberDisplayWidget(Coordinate.custom(0, 11, 0.25, 21), menu.temperatureStorageJ, 7, 1, true).mainColor(-1));
            addChild(new NumberDisplayWidget(Coordinate.custom(0, 11, 0.5 , 13), menu.momentumStorageJ   , 7, 1, true).mainColor(-1));
            addChild(new NumberDisplayWidget(Coordinate.custom(0, 11, 0.75, 5 ), menu.pressureStorageJ   , 7, 1, true).mainColor(-1));

            addChild(new TextWidget(Coordinate.custom(0, 54, 0   , 30), Component.literal("J")).noShadow().mainColor(-1));
            addChild(new TextWidget(Coordinate.custom(0, 54, 0.25, 22), Component.literal("J")).noShadow().mainColor(-1));
            addChild(new TextWidget(Coordinate.custom(0, 54, 0.5 , 14), Component.literal("J")).noShadow().mainColor(-1));
            addChild(new TextWidget(Coordinate.custom(0, 54, 0.75, 6 ), Component.literal("J")).noShadow().mainColor(-1));

            // 总存储
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -74, 0   , 29), menu.radiationCacheJ  , 7, 1, true).rightAlign().mainColor(-1));
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -74, 0.25, 21), menu.temperatureCacheJ, 7, 1, true).rightAlign().mainColor(-1));
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -74, 0.5 , 13), menu.momentumCacheJ   , 7, 1, true).rightAlign().mainColor(-1));
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -74, 0.75, 5 ), menu.pressureCacheJ   , 7, 1, true).rightAlign().mainColor(-1));

            addChild(new TextWidget(Coordinate.custom(1, -67, 0   , 30), Component.literal("J")).noShadow().rightAlign().mainColor(-1));
            addChild(new TextWidget(Coordinate.custom(1, -67, 0.25, 22), Component.literal("J")).noShadow().rightAlign().mainColor(-1));
            addChild(new TextWidget(Coordinate.custom(1, -67, 0.5 , 14), Component.literal("J")).noShadow().rightAlign().mainColor(-1));
            addChild(new TextWidget(Coordinate.custom(1, -67, 0.75, 6 ), Component.literal("J")).noShadow().rightAlign().mainColor(-1));

            // 净功率
            addChild(new RectangleWidget(Coordinate.custom(1, -7, 0   , 29), Coordinate.fromTopLeft(53, 9)).rightAlign().mainColor(0xbfffff00));
            addChild(new RectangleWidget(Coordinate.custom(1, -7, 0.25, 21), Coordinate.fromTopLeft(53, 9)).rightAlign().mainColor(0xbfff0000));
            addChild(new RectangleWidget(Coordinate.custom(1, -7, 0.5 , 13), Coordinate.fromTopLeft(53, 9)).rightAlign().mainColor(0xbf00ffff));
            addChild(new RectangleWidget(Coordinate.custom(1, -7, 0.75, 5 ), Coordinate.fromTopLeft(53, 9)).rightAlign().mainColor(0xbf00ff00));

            addChild(new NumberDisplayWidget(Coordinate.custom(1, -16, 0   , 29), menu.radiationPowerW  , 7, 1, true).rightAlign().mainColor(0xff000000));
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -16, 0.25, 21), menu.temperaturePowerW, 7, 1, true).rightAlign().mainColor(0xff000000));
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -16, 0.5 , 13), menu.momentumPowerW   , 7, 1, true).rightAlign().mainColor(0xff000000));
            addChild(new NumberDisplayWidget(Coordinate.custom(1, -16, 0.75, 5 ), menu.pressurePowerW   , 7, 1, true).rightAlign().mainColor(0xff000000));

            addChild(new TextWidget(Coordinate.custom(1, -9, 0   , 30), Component.literal("W")).noShadow().rightAlign().mainColor(0xff000000));
            addChild(new TextWidget(Coordinate.custom(1, -9, 0.25, 22), Component.literal("W")).noShadow().rightAlign().mainColor(0xff000000));
            addChild(new TextWidget(Coordinate.custom(1, -9, 0.5 , 14), Component.literal("W")).noShadow().rightAlign().mainColor(0xff000000));
            addChild(new TextWidget(Coordinate.custom(1, -9, 0.75, 6 ), Component.literal("W")).noShadow().rightAlign().mainColor(0xff000000));

            // 进度条
            addChild(new ProgressBarWidget(Coordinate.custom(0, 11, 0.25, 18), Coordinate.custom(1, -18, 0.25, -21), menu.radiationStorageJ  , menu.radiationCacheJ  ).bottomAlignY().mainColor(0xffffff00).bgColor(0x0fffff00));
            addChild(new ProgressBarWidget(Coordinate.custom(0, 11, 0.5 , 10), Coordinate.custom(1, -18, 0.25, -21), menu.temperatureStorageJ, menu.temperatureCacheJ).bottomAlignY().mainColor(0xffff0000).bgColor(0x0fff0000));
            addChild(new ProgressBarWidget(Coordinate.custom(0, 11, 0.75, 2 ), Coordinate.custom(1, -18, 0.25, -21), menu.momentumStorageJ   , menu.momentumCacheJ   ).bottomAlignY().mainColor(0xff00ffff).bgColor(0x0f00ffff));
            addChild(new ProgressBarWidget(Coordinate.custom(0, 11, 1   , -6), Coordinate.custom(1, -18, 0.25, -21), menu.pressureStorageJ   , menu.pressureCacheJ   ).bottomAlignY().mainColor(0xff00ff00).bgColor(0x0f00ff00));
        }
    }

    public static class MachineControlWindow extends InformationWindowWidget {
        public MachineMenu menu;

        public MachineControlWindow(Coordinate pos, Coordinate size, MachineMenu menu) {
            super(pos, size, Component.literal("机器控制"), 120, 40);
            this.menu = menu;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            var switchWidget = new SwitchWidget(Coordinate.fromTopLeft(7, 15), Coordinate.fromTopLeft(50, 19), Component.literal("关闭"), Component.literal("开启"));
            addChild(switchWidget.mainColor(-1));

            menu.enabled.whenFirstDataArrivesDo(() -> {
                switchWidget.pressed = menu.enabled.get();
                switchWidget.rectDx.set(switchWidget.pressed ? (double) switchWidget.w() / 2 : 0);
            });
            switchWidget.onSwitch(pressed -> menu.powerSwitch.trigger(pressed));
        }
    }

}
