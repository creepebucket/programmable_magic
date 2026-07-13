package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.*;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.utils.ModColors;
import org.creepebucket.programmable_magic.utils.ModUtils;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.*;

public class MachineWidgets {
    public static class WindowHintWidget extends Widget implements Lifecycle, Tickable, KeyInputable {
        public Widget hintText;

        public WindowHintWidget(Coordinate pos) {
            super(pos, ZERO);
        }

        @Override
        public void onInitialize() {
            hintText = screen.addWidget(new TextWidget(fromCenter(0, 0), literal("按下 Alt+W 开关窗口管理界面")).centerAlign().centerAlignY().mainColor(-1).disable());
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
        public TextWidget titleWidget;
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

                targetX = Math.clamp(targetX, -originalX(), getScreenW() - originalX() - w());
                targetY = Math.clamp(targetY, -originalY(), getScreenH() - originalY() - h());

                dx.set(targetX);
                dy.set(targetY);
                return true;
            }
            else if (changingSize) {
                var targetX = dw.target + dragX;
                var targetY = dh.target + dragY;

                targetX = Math.clamp(targetX, minW - originalW(), getScreenW() - originalX() - originalW());
                targetY = Math.clamp(targetY, minH - originalH(), getScreenH() - originalY() - originalH());

                dw.set(targetX);
                dh.set(targetY);

                onResize((int) targetX + originalW(), (int) targetX + originalH());

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

            addChild(new RectangleWidget(fromTopLeft(0, 0), fromTopRight(0, 11)).mainColor(0xbf000000));
            titleWidget = (TextWidget) addChild(new TextWidget(fromTopLeft(16, 2), name).noShadow().mainColor(0xff7f7f7f));
            addChild(new RectangleWidget(fromTopLeft(3, 5), fromTopLeft(10, 1)).mainColor(0x7f7f7f7f));
            addChild(new RectangleWidget(fromTopLeft(titleWidget.w() + 18, 5), fromTopRight(-titleWidget.w() - 22, 1)).mainColor(0x7f7f7f7f));
            closeButton = addChild(new RectangleWidget(fromTopRight(-7, 4), fromTopLeft(3, 3)));

            addChild(new RectangleWidget(fromTopLeft(0, 11), fromBottomRight(0, -11)).mainColor(0x7f000000));

            addAnimation(new Animation.FadeIn.FromTop(0.3), (double) ModUtils.simpleRandInt(0, 2) / 20);
            onResize(w(), h());
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
                parent.children.add(this);
            }

            if (isIn(event.x(), event.y(), right() - 9, top() + 2, 7, 7)) {
                addAnimation(new Animation.FadeOut.ToBottom(0.3).noDeletion(), 0);
                return true;
            }
            else if (isIn(event.x(), event.y(), x(), y(), w(), 11)) {
                changingPosition = true;
                return true;
            }
            else if (isIn(event.x(), event.y(), right() - 5, bottom() - 5, 5, 5)) {
                changingSize = true;
                return true;
            }
		    else if (enabled && isInBounds(event.x(), event.y())) {
                // 只在子组件分发事件, 阻断传播
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
                graphics.fill(right() - 4, bottom() + 1, right(), bottom(), -1);
                graphics.fill(right() + 1, bottom() - 4, right(), bottom(), -1);
            }
            else if (isIn(mouseX, mouseY, right() - 9, top() + 2, 7, 7)) {
                closeButton.mainColor(ModColors.MAIN_COLOR_T);
            }
        }

        public void onResize(int width, int height) {}
    }

    public static class WindowManagementWindow extends InformationWindowWidget implements Tickable{
        public List<SwitchWidget> switches = new ArrayList<>();

        public WindowManagementWindow(Coordinate pos, Coordinate size) {
            super(pos, size, literal("窗口管理"), 120, 60);
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new TextWidget(fromTopLeft(7, 15), literal("窗口名称")).noShadow().mainColor(0xffbfbfbf));
            addChild(new TextWidget(fromTopRight(-16, 15), literal("开关")).noShadow().rightAlign().mainColor(0xffbfbfbf));

            addChild(new RectangleWidget(fromTopLeft(7, 26), fromTopRight(-14, 1)).mainColor(mainColor().toArgbWithAlphaMult(0.5)));

            var cull = addChild(new CullAreaWidget(fromTopLeft(7, 28), fromBottomRight(-14, -35)));
            var itemDy = new SmoothedValue(0);
            smoothedValues.add(itemDy);
            var y = 0;

            for (InformationWindowWidget window: ((MachineScreen<?>) screen).windows) {
                cull.addChild(new TextWidget(fromTopLeft(0, 3 + y), window.name).noShadow().mainColor(-1).dy(itemDy));
                switches.add((SwitchWidget) cull.addChild(new SwitchWidget(fromTopRight(-4, 1 + y), fromTopLeft(60, 11), literal("关闭"),
                        literal("开启")).setPressed(window.enabled).onSwitch(b -> {if (b) window.enable(); else window.disable();}).rightAlign().dy(itemDy)));
                y += 15;
            }

            cull.addChild(new ScrollbarWidget(fromTopRight(-3, 1), fromBottomLeft(3, -2), fromTopLeft(-y, 0), itemDy, "y").reverseDirection());
        }

        @Override
        public void tick() {
            var windows = ((MachineScreen<?>) screen).windows;
            for (int i = 0; i < switches.size(); i++) {
                switches.get(i).setPressed(windows.get(i).enabled && windows.get(i).animations.stream().noneMatch(animation -> animation instanceof Animation.FadeOut));
            }
        }
    }

    public static class PowerInfoItemWidget extends Widget implements Lifecycle, Clickable {
        public Component name, unit;
        public DynamicValue<Double> value;

        public PowerInfoItemWidget(Coordinate pos, Component name, DynamicValue<Double> value, Component unit) {
            super(pos, ZERO);
            
            this.value = value;
            this.name = name;
            this.unit = unit;
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(fromTopLeft(0, 0), fromTopLeft(1, 19)).mainColor(0xff7f7f7f));
            var a = addChild(new TextWidget(fromTopLeft(3, 9), name).noShadow().mainColor(0xffbfbfbf).bottomAlignY());
            addChild(new NumberDisplayWidget(fromTopLeft(3, 10), value, 6, 1, true));
            var b = addChild(new TextWidget(fromTopLeft(41, 11), unit).noShadow().mainColor(-1));
            
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
                pos = fromTopLeft(7, 18 + row * 25);
            else
                pos = fromCenterTop(7, 18 + row * 25);
            var item = new PowerInfoItemWidget(pos, name, value, unit);
            addChild(item);
            return item;
        }

        public PowerInfoWindow(Coordinate pos, Coordinate size, Component powerExpr) {
            super(pos, size, literal("计算信息"), 150, 50);
            this.powerExpr = powerExpr;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new RectangleWidget(fromBottomLeft(7, -7), fromTopRight(-14, 18)).mainColor(0x7f000000).bottomAlignY());
            addChild(new TextWidget(fromCenterBottom(0, -11), powerExpr).noShadow().centerAlign().bottomAlignY().mainColor(0xffbfbfbf));
        }
    }

    public static class MachineInfoWindow extends InformationWindowWidget {
        public DynamicValue<Double> power;
        public Component manaType, machineType, mainText1, mainText2;
        public String unit;

        public MachineInfoWindow(Coordinate pos, Coordinate size, DynamicValue power, Component manaType, Component machineType, Component mainText1, Component mainText2, String unit) {
            super(pos, size, literal("机器总览"), 210, 40);

            this.manaType = manaType;
            this.power = power;
            this.machineType = machineType;
            this.mainText1 = mainText1;
            this.mainText2 = mainText2;
            this.unit = unit;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new TextWidget(fromTopLeft(7, 39), literal("机器类型")).noShadow().bottomAlignY().mainColor(0xff7f7f7f));
            addChild(new TextWidget(fromTopLeft(7, 15), machineType).scaled(1.5));
            addChild(new TextWidget(fromTopRight(-7, 39), literal("魔力类型")).noShadow().rightAlign().bottomAlignY().mainColor(0xff7f7f7f));
            addChild(new TextWidget(fromTopRight(-7, 15), manaType).scaled(1.5).rightAlign());
            addChild(new TextWidget(fromCenterBottom(0, -33), mainText1).centerAlign().bottomAlignY().mainColor(0xff7f7f7f));
            addChild(new TextWidget(fromCenterBottom(-72, -5), mainText2).noShadow().scaled(2).rightAlign().bottomAlignY().mainColor(0xffffffff));
            var unit = new TextSwitchWidget(fromCenterBottom(73, -5), fromTopLeft(26, 18), 2, this.unit);
            addChild(unit.bottomAlignY().mainColor(0xffffffff));
            addChild(new NumberDisplayWidget(fromCenterBottom(0, -5), power, 6, 3, unit, this.unit, false).centerAlign().bottomAlignY());
        }
    }

    public static class NetworkInfoWindow extends InformationWindowWidget {
        public MachineMenu menu;

        public NetworkInfoWindow(Coordinate pos, Coordinate size, MachineMenu menu) {
            super(pos, size, literal("网络信息"), 180, 90);
            this.menu = menu;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new RectangleWidget(fromTopLeft(7, 14), fromTopLeft(2, 10)).mainColor(0xbfbfbfbf));
            addChild(new TextWidget(fromTopLeft(11, 15), literal("当前魔力")).noShadow().mainColor(0xffbfbfbf));
            addChild(new TextWidget(fromTopRight(-66, 15), literal("魔力缓存")).noShadow().rightAlign().mainColor(0xffbfbfbf));
            addChild(new TextWidget(fromTopRight(-7, 15), literal("净功率")).noShadow().rightAlign().mainColor(0xffbfbfbf));

            addChild(new RectangleWidget(custom(0, 7, 0   , 29), custom(0, 2, 0.25, -11)).mainColor(ModColors.MAIN_COLOR_R));
            addChild(new RectangleWidget(custom(0, 7, 0.25, 21), custom(0, 2, 0.25, -11)).mainColor(ModColors.MAIN_COLOR_T));
            addChild(new RectangleWidget(custom(0, 7, 0.5 , 13), custom(0, 2, 0.25, -11)).mainColor(ModColors.MAIN_COLOR_M));
            addChild(new RectangleWidget(custom(0, 7, 0.75, 5 ), custom(0, 2, 0.25, -11)).mainColor(ModColors.MAIN_COLOR_P));

            addChild(new NumberDisplayWidget(custom(0, 11, 0   , 29), menu.radiationStorageJ  , 7, 1, true).mainColor(-1));
            addChild(new NumberDisplayWidget(custom(0, 11, 0.25, 21), menu.temperatureStorageJ, 7, 1, true).mainColor(-1));
            addChild(new NumberDisplayWidget(custom(0, 11, 0.5 , 13), menu.momentumStorageJ   , 7, 1, true).mainColor(-1));
            addChild(new NumberDisplayWidget(custom(0, 11, 0.75, 5 ), menu.pressureStorageJ   , 7, 1, true).mainColor(-1));

            addChild(new TextWidget(custom(0, 54, 0   , 30), literal("J")).noShadow().mainColor(-1));
            addChild(new TextWidget(custom(0, 54, 0.25, 22), literal("J")).noShadow().mainColor(-1));
            addChild(new TextWidget(custom(0, 54, 0.5 , 14), literal("J")).noShadow().mainColor(-1));
            addChild(new TextWidget(custom(0, 54, 0.75, 6 ), literal("J")).noShadow().mainColor(-1));

            addChild(new NumberDisplayWidget(custom(1, -74, 0   , 29), menu.radiationCacheJ  , 7, 1, true).rightAlign().mainColor(-1));
            addChild(new NumberDisplayWidget(custom(1, -74, 0.25, 21), menu.temperatureCacheJ, 7, 1, true).rightAlign().mainColor(-1));
            addChild(new NumberDisplayWidget(custom(1, -74, 0.5 , 13), menu.momentumCacheJ   , 7, 1, true).rightAlign().mainColor(-1));
            addChild(new NumberDisplayWidget(custom(1, -74, 0.75, 5 ), menu.pressureCacheJ   , 7, 1, true).rightAlign().mainColor(-1));

            addChild(new TextWidget(custom(1, -67, 0   , 30), literal("J")).noShadow().rightAlign().mainColor(-1));
            addChild(new TextWidget(custom(1, -67, 0.25, 22), literal("J")).noShadow().rightAlign().mainColor(-1));
            addChild(new TextWidget(custom(1, -67, 0.5 , 14), literal("J")).noShadow().rightAlign().mainColor(-1));
            addChild(new TextWidget(custom(1, -67, 0.75, 6 ), literal("J")).noShadow().rightAlign().mainColor(-1));

            addChild(new RectangleWidget(custom(1, -7, 0   , 29), fromTopLeft(53, 9)).rightAlign().mainColor(ModColors.MAIN_COLOR_R));
            addChild(new RectangleWidget(custom(1, -7, 0.25, 21), fromTopLeft(53, 9)).rightAlign().mainColor(ModColors.MAIN_COLOR_T));
            addChild(new RectangleWidget(custom(1, -7, 0.5 , 13), fromTopLeft(53, 9)).rightAlign().mainColor(ModColors.MAIN_COLOR_M));
            addChild(new RectangleWidget(custom(1, -7, 0.75, 5 ), fromTopLeft(53, 9)).rightAlign().mainColor(ModColors.MAIN_COLOR_P));

            addChild(new NumberDisplayWidget(custom(1, -16, 0   , 29), menu.radiationPowerW  , 7, 1, true).rightAlign().mainColor(0xff000000));
            addChild(new NumberDisplayWidget(custom(1, -16, 0.25, 21), menu.temperaturePowerW, 7, 1, true).rightAlign().mainColor(0xff000000));
            addChild(new NumberDisplayWidget(custom(1, -16, 0.5 , 13), menu.momentumPowerW   , 7, 1, true).rightAlign().mainColor(0xff000000));
            addChild(new NumberDisplayWidget(custom(1, -16, 0.75, 5 ), menu.pressurePowerW   , 7, 1, true).rightAlign().mainColor(0xff000000));

            addChild(new TextWidget(custom(1, -9, 0   , 30), literal("W")).noShadow().rightAlign().mainColor(0xff000000));
            addChild(new TextWidget(custom(1, -9, 0.25, 22), literal("W")).noShadow().rightAlign().mainColor(0xff000000));
            addChild(new TextWidget(custom(1, -9, 0.5 , 14), literal("W")).noShadow().rightAlign().mainColor(0xff000000));
            addChild(new TextWidget(custom(1, -9, 0.75, 6 ), literal("W")).noShadow().rightAlign().mainColor(0xff000000));

            addChild(new ProgressBarWidget(custom(0, 11, 0.25, 18), custom(1, -18, 0.25, -21), menu.radiationStorageJ  , menu.radiationCacheJ  ).bottomAlignY().mainColor(ModColors.MAIN_COLOR_R).bgColor(ModColors.MAIN_COLOR_R.withAlpha(0x0f)));
            addChild(new ProgressBarWidget(custom(0, 11, 0.5 , 10), custom(1, -18, 0.25, -21), menu.temperatureStorageJ, menu.temperatureCacheJ).bottomAlignY().mainColor(ModColors.MAIN_COLOR_T).bgColor(ModColors.MAIN_COLOR_T.withAlpha(0x0f)));
            addChild(new ProgressBarWidget(custom(0, 11, 0.75, 2 ), custom(1, -18, 0.25, -21), menu.momentumStorageJ   , menu.momentumCacheJ   ).bottomAlignY().mainColor(ModColors.MAIN_COLOR_M).bgColor(ModColors.MAIN_COLOR_M.withAlpha(0x0f)));
            addChild(new ProgressBarWidget(custom(0, 11, 1   , -6), custom(1, -18, 0.25, -21), menu.pressureStorageJ   , menu.pressureCacheJ   ).bottomAlignY().mainColor(ModColors.MAIN_COLOR_P).bgColor(ModColors.MAIN_COLOR_P.withAlpha(0x0f)));
        }
    }

    public static class MachineControlWindow extends InformationWindowWidget {
        public MachineMenu menu;

        public MachineControlWindow(Coordinate pos, Coordinate size, MachineMenu menu) {
            super(pos, size, literal("机器控制"), 60, 40);
            this.menu = menu;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            var switchWidget = new SwitchWidget(fromTopLeft(7, 16), fromTopLeft(50, 19), literal("关闭"), literal("开启"), menu.enabled).onSwitch(enabled -> menu.powerSwitch.trigger(enabled));
            addChild(switchWidget.mainColor(-1));
        }
    }

    public static class OverclockWindow extends InformationWindowWidget {
        public DynamicValue<Double> powerFact;
        public double basePower, baseControl, maxFact;
        public Widget arrowWidget;
        public List<Widget> graduations = new ArrayList<>();

        public OverclockWindow(Coordinate pos, Coordinate size, DynamicValue<Double> powerFact, double basePower, double baseControl, double maxFact) {
            super(pos, size, literal("生产控制"), 165, 40);
            this.powerFact = powerFact;
            this.basePower = basePower;
            this.baseControl = baseControl;
            this.maxFact = maxFact;
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            addChild(new TextWidget(fromTopLeft(7, 20), literal("超频倍率")).noShadow().mainColor(-1));
            addChild(new NumberDisplayWidget(fromTopLeft(44, 16), powerFact, 4, 1.5, true));

            addChild(new TextWidget(fromTopRight(-45, 24), literal("预期功率 / W")).scaled(0.5).noShadow().rightAlign().mainColor(0x7fffffff));
            addChild(new NumberDisplayWidget(fromTopRight(-45, 16), DynamicValue.fromSupplier(() -> basePower * powerFact.get()), 6, 1, true).rightAlign());

            addChild(new TextWidget(fromTopRight(-7, 24), literal("控制成本 / W")).scaled(0.5).noShadow().rightAlign().mainColor(0x7fffffff));
            addChild(new NumberDisplayWidget(fromTopRight(-7, 16), DynamicValue.fromSupplier(() -> Math.pow(4, powerFact.get() - 1) * baseControl), 6, 1, true).rightAlign().mainColor(ModColors.MAIN_COLOR_R));

            powerFact.whenFirstDataArrivesDo(() -> addChild(new ThinSlideBarWidget(fromBottomLeft(7, -9), fromTopRight(-14, 5), 0, 4, powerFact).step(0.05).bgColor(-1)));

            // 添加刻度
            for (int i = 1; i < maxFact; i++) {
                graduations.add(addChild(new TextWidget(custom((double) i / (int) maxFact, (int) Math.floor(-14 * (double) i / (int) maxFact + 7), 1, -13), literal(String.valueOf(i))).scaled(0.5).noShadow().centerAlign().mainColor(0x7fffffff)));
            }
            graduations.add(addChild(new TextWidget(custom(0, 7, 1, -13), literal("0")).scaled(0.5).noShadow().mainColor(0x7fffffff)));
            graduations.add(addChild(new TextWidget(custom(1, -6, 1, -13), literal(String.valueOf((int) maxFact))).scaled(0.5).noShadow().rightAlign().mainColor(0x7fffffff)));
        }
    }

    public static class FluidInfoWidget extends Widget implements Lifecycle {
        public DynamicValue<String> fluidId;
        public FluidTextureWidget fluidTexture;
        public TextWidget fluidNameText;
        public TextWidget fluidIdText;

        public FluidInfoWidget(Coordinate pos, DynamicValue<String> fluidId) {
            super(pos, ZERO);
            this.fluidId = fluidId;
        }

        @Override
        public void onInitialize() {
            addChild(new TextWidget(fromTopLeft(0, 0), literal("流体类型")).noShadow().mainColor(0xff7f7f7f));
            fluidTexture = (FluidTextureWidget) addChild(new FluidTextureWidget(fromTopLeft(0, 12), fromTopLeft(10, 10), fluidId.get()));
            fluidNameText = (TextWidget) addChild(new TextWidget(fromTopLeft(13, 13), fluidId.get().isEmpty() ? literal("") :
                    Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidId.get())).getFluidType().getDescriptionId())).noShadow());
            fluidIdText = (TextWidget) addChild(new TextWidget(fromTopLeft(0, 25), literal(fluidId.get())).scaled(0.5).noShadow().mainColor(0xff7f7f7f));

            fluidId.whenDataChangedDo(() -> {
                fluidTexture.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
                fluidNameText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
                fluidIdText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

                fluidTexture = (FluidTextureWidget) addChild(new FluidTextureWidget(fromTopLeft(0, 12), fromTopLeft(10, 10), fluidId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
                fluidNameText = (TextWidget) addChild(new TextWidget(fromTopLeft(13, 13), fluidId.get().isEmpty() ? literal("") :
                        Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidId.get())).getFluidType().getDescriptionId())).noShadow().addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
                fluidIdText = (TextWidget) addChild(new TextWidget(fromTopLeft(0, 25), literal(fluidId.get())).scaled(0.5).noShadow().mainColor(0xff7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
            });
        }
    }

    public static class InventoryWindow extends InformationWindowWidget {

        public InventoryWindow(Coordinate pos, Coordinate size) {
            super(pos, size, literal("物品栏"), 184, 67);
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            var slots = screen.getMenu().slots;
            for (int i = 0; i < 3; i++) for (int j = -4; j < 5; j++) {
                addChild(new RectangleWidget(fromCenterTop(j * 18, i * 18 + 30), fromTopLeft(16, 1)).centerAlign().mainColor(0x1fffffff));
                addChild(new SlotWidget(slots.get(i * 9 + j + 13), fromCenterTop(j * 18, i * 18 + 15)).centerAlign());
            }
            for (int j = -4; j < 5; j++) {
                addChild(new RectangleWidget(fromCenterBottom(j * 18, -4), fromTopLeft(16, 1)).centerAlign().bottomAlignY().mainColor(0x1fffffff));
                addChild(new SlotWidget(slots.get(j + 4), fromCenterBottom(j * 18, -4)).centerAlign().bottomAlignY());
            }
        }
    }

}
