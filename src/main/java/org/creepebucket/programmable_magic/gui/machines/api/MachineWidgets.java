package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;
import org.creepebucket.programmable_magic.gui.lib.widgets.ProgressBarWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SwitchWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;

import java.util.ArrayList;
import java.util.List;

public class MachineWidgets {
    /**
     * 纯ai （注：可能是作者留下的注释，暗示这部分逻辑非常巧妙/自动化）
     * 整体数字显示屏组件，包含多个单数字体（Digit）和一个单位显示器
     */
    public static class NumberDisplayWidget extends Widget implements Renderable, Tickable {
        public DynamicValue<Double> number; // 与服务端同步的数值
        public int digits;                 // 支持显示的最大位数（如8位显示屏）
        public List<NumberDigitWidget> digit = new ArrayList<>(); // 存放每一个单数字符的列表
        public double scale;                  // 缩放比例
        public TextSwitchWidget unitWidget;// 负责显示和切换单位的组件（如 FE, kFE）
        public String baseUnit;            // 基础单位名称（如 "FE"）
        public boolean compactMode;        // 紧凑模式
        public boolean compactUnit = false;        // 单位不在TextSwitchUnit中显示

        public NumberDisplayWidget(Coordinate pos, DynamicValue<Double> number, int digits, double scale, TextSwitchWidget unitWidget, String baseUnit, Boolean compactMode) {
            // 初始化大小：宽度 = (8 * 缩放 * 位数 - 缩放) [计算出刚好容纳所有数字的宽度]，高度 = 9 * 缩放
            super(pos, Coordinate.fromTopLeft((int) ((compactMode ? 7 : 8) * scale * digits - scale), (int) (9 * scale)));
            this.number = number;
            this.digits = digits;
            this.scale = scale;
            this.unitWidget = unitWidget;
            this.baseUnit = baseUnit;
            this.compactMode = compactMode;

            // 根据指定的位数，生成对应的单数字组件，并作为子组件添加

            for (int i = 0; i < digits; i++)
                digit.add((NumberDigitWidget) addChild(new NumberDigitWidget(
                        Coordinate.fromTopLeft((int) (scale + (compactMode ? 6 : 8) * scale * i), (int) scale), // 错开 X 坐标排列
                        Coordinate.fromTopLeft(40, 50), scale)));
        }

        public NumberDisplayWidget(Coordinate pos, DynamicValue<Double> number, int digits, double scale, Boolean compactMode) {
            // 初始化大小：宽度 = (8 * 缩放 * 位数 - 缩放) [计算出刚好容纳所有数字的宽度]，高度 = 9 * 缩放
            super(pos, Coordinate.fromTopLeft((int) ((compactMode ? 7 : 8) * scale * digits - scale), (int) (9 * scale)));
            this.number = number;
            this.digits = digits;
            this.scale = scale;
            this.compactMode = compactMode;
            this.compactUnit = true;

            // 根据指定的位数，生成对应的单数字组件，并作为子组件添加

            for (int i = 0; i < digits; i++)
                digit.add((NumberDigitWidget) addChild(new NumberDigitWidget(
                        Coordinate.fromTopLeft((int) (scale + (compactMode ? 6 : 8) * scale * i), (int) scale), // 错开 X 坐标排列
                        Coordinate.fromTopLeft(40, 50), scale)));
        }

        @Override
        public void renderWidget(GuiGraphicsExtractor graphics, int mx, int my, float partialTick, double dt, boolean isForeground) {
            // 开启 OpenGL 裁剪：由于子组件（数字）在滚动时会超出自身框的范围
            // 这里设置裁剪区，确保只有在当前组件区域内的像素才会被渲染出，达到“遮罩”效果
            graphics.enableScissor(left(), top(), right(), bottom());
            super.renderWidget(graphics, mx, my, partialTick, dt, isForeground);
            // 渲染完毕后关闭裁剪，以免影响其他 GUI 元素的渲染
            graphics.disableScissor();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            // 为每一个数字槽位绘制背景色块
            if (!compactMode) for (int i = 0; i < digits; i++)
                graphics.fill((int) (left() + 8 * i * scale), top(), (int) (left() + 8 * i * scale + 7 * scale), bottom(), bgColorInt());
        }

        @Override
        public void tick() {
            // 每一Tick更新数值逻辑
            // 将双精度浮点数格式化为带单位的字符串，如 "1.23 k" 或 "450 "
            var number = this.number.get();
            String formatted = ModUtils.formattedNumber(number, number >= 1000 && number < 1e27 && !compactUnit ? digits + 2 : digits + 1);
            int sep = formatted.lastIndexOf(' '); // 找到数字和单位之间的空格

            // 提取数字部分（尾数）和单位前缀（k, M, G 等）
            String mantissa = sep == -1 ? formatted : formatted.substring(0, sep);
            String prefix = sep == -1 ? "" : formatted.substring(sep + 1);

            formatted = mantissa + prefix;
            // 如果单位发生了变化，通知单位组件执行切换动画
            if (!compactUnit && !unitWidget.current.equals(prefix + baseUnit)) unitWidget.switchText(prefix + baseUnit);
            // 将数字字符串从后往前（从低位到高位）塞入对应的单字符显示器中
            var text = (!compactUnit ? mantissa : formatted);
            text = (text + " ".repeat(digits)).substring(0, digits);
            for (int i = 0; i < digits; i++) {
                digit.get(i).setChar(text.charAt(i));
            }
        }

        /**
         * 负责处理单个字符（0-9, 小数点, 空格）渲染和滚动动画的内部类
         */
        public static class NumberDigitWidget extends Widget implements Renderable {
            public int digit = 0;
            public double scale;
            public char old = ' ';        // 切换动画时的旧字符
            public char current = ' ';    // 当前字符
            public SmoothedValue textDy = new SmoothedValue(0); // 用于非数字字符切换时的垂直平滑位移
            public boolean isSwitching = false; // 是否正在进行非数字的切换动画

            public NumberDigitWidget(Coordinate pos, Coordinate size, double scale) {
                super(pos, size);
                this.scale = scale;
            }

            @Override
            public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                int lineHeight = (int) (scale * 10); // 每个字符占据的高度
                // dy 是父类 Widget 里的变量，这里被用来表示滚动纸带的总偏移量
                int baseY = menuY() - dy.getInt();
                // 状态1：正在进行非数字切换动画（例如 空格 -> 1, 或者 5 -> .）
                if (isSwitching) {
                    textDy.doStep(screen.dt); // 更新平滑动画步长

                    // 绘制旧字符（逐渐滑出）
                    if (old != ' ')
                        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(old)), menuX(), baseY - lineHeight + textDy.getInt(), (float) scale, mainColorInt(), false);

                    // 绘制新字符（逐渐滑入）
                    if (current != ' ')
                        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(current)), menuX(), baseY + textDy.getInt(), (float) scale, mainColorInt(), false);

                    // 动画结束后的状态重置
                    if (textDy.getInt() == 0) {
                        isSwitching = false;
                        // 如果切换后的新字符是数字，则将全局偏移量 dy 瞬间对齐到该数字对应的正确高度
                        if (current >= '0' && current <= '9') dy.setImmediate(-lineHeight * digit);
                    }
                    return;
                }
                // 状态2：当前字符是非数字（如小数点或空格），静止绘制即可
                if (current < '0' || current > '9') {
                    if (current != ' ')
                        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(current)), menuX(), baseY, (float) scale, mainColorInt(), false);
                    return;
                }
                // 状态3：当前字符是数字 0-9，执行数字卷轴渲染逻辑
                // 绘制一列 0 到 9，再加上一个 0 （总共11个），方便 9 向下无缝滚动到 0
                for (int i = 0; i < 11; i++)
                    TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(i % 10)), menuX(), menuY() + lineHeight * i, (float) scale, mainColorInt(), false);
                // ===== 无限循环滚动逻辑 =====
                // 当纸带向上滚动超出范围（10个数字的高度即 scale * 100）时，将真实位置和目标位置同时拉回一圈
                if (dy.getInt() < -scale * 100) {
                    dy.target += scale * 100;
                    dy.current += scale * 100;
                }
                // 当纸带向下滚动超出范围时，类似处理
                if (dy.getInt() > 0) {
                    dy.target -= scale * 100;
                    dy.current -= scale * 100;
                }
            }

            // 当目标是纯数字之间的切换时调用
            public void setDigit(int n) {
                int lineHeight = (int) (scale * 10); // 每个字符占据的高度

                // 计算数字环（0-9）上，向上滚和向下滚的距离
                var nCoord = -n * lineHeight;
                var targetCoord = dy.target % (lineHeight * 10);

                var upDiff = Math.min((nCoord - targetCoord), (nCoord - targetCoord - lineHeight * 10) % (lineHeight * 10));

                dy.set(upDiff < -lineHeight * 5 ? dy.target + upDiff + 10 * lineHeight : dy.target + upDiff);
                digit = n; // 更新当前数字
            }

            // 核心的字符状态改变逻辑
            public void setChar(char ch) {
                boolean newIsDigit = ch >= '0' && ch <= '9';
                boolean oldIsDigit = (isSwitching ? old : current) >= '0' && (isSwitching ? old : current) <= '9';
                // 情况A：从一个数字切换到另一个数字 -> 使用卷轴滚动动画
                if (newIsDigit && oldIsDigit) {
                    isSwitching = false;
                    old = ch;
                    current = ch;
                    setDigit(ch - '0'); // 触发卷轴滚动目标变更
                    return;
                }
                if (ch == current) return; // 没有变化，直接返回
                // 情况B：涉及非数字字符的切换（如 5 -> . 或 空格 -> 1）-> 使用推拉切换动画
                if (newIsDigit) digit = ch - '0';
                textDy.setImmediate(scale * 10); // 设置动画起点位置
                textDy.set(0);                   // 设置动画终点位置为0（触发平滑过渡）
                old = current;
                current = ch;
                isSwitching = true; // 标记正在进行切换动画
            }
        }
    }

    public static class TextSwitchWidget extends Widget implements Renderable {
        public String old = "";
        public String current = "";
        public int scale;
        public SmoothedValue textDy = new SmoothedValue(0);

        public TextSwitchWidget(Coordinate pos, Coordinate size, int scale, String initial) {
            super(pos, size);

            this.current = initial;
            this.scale = scale;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            textDy.doStep(screen.dt);

            graphics.enableScissor(left(), top(), right(), bottom());

            graphics.fill(left(), top(), right(), bottom(), bgColorInt());
            TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(old), left() + scale, top() + scale - h() + textDy.getInt(), scale, mainColorInt(), false);
            TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(current), left() + scale, top() + scale + textDy.getInt(), scale, mainColorInt(), false);

            graphics.disableScissor();
        }

        public void switchText(String newText) {
            textDy.setImmediate(h());
            textDy.set(0);

            old = current;
            current = newText;
        }
    }

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
            addChild(new TextWidget(Coordinate.fromTopLeft(w() - 1, 2), resultUnit).noShadow().rightAlign().mainColor(textColor()));
            // 描述
            descWidget = addChild(new RectangleWidget(Coordinate.fromTopLeft(0, -1), Coordinate.fromTopLeft(w(), 4)).bottomAlignY());
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
}
