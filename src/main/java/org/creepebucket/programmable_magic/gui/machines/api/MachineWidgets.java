package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;

import java.util.ArrayList;
import java.util.List;

public class MachineWidgets {
    /**
     * 纯ai （注：可能是作者留下的注释，暗示这部分逻辑非常巧妙/自动化）
     * 整体数字显示屏组件，包含多个单数字体（Digit）和一个单位显示器
     */
    public static class NumberDisplayWidget extends Widget implements Renderable, Tickable {
        public SyncedValue<Double> number; // 与服务端同步的数值
        public int digits;                 // 支持显示的最大位数（如8位显示屏）
        public List<NumberDigitWidget> digit = new ArrayList<>(); // 存放每一个单数字符的列表
        public int scale;                  // 缩放比例
        public TextSwitchWidget unitWidget;// 负责显示和切换单位的组件（如 FE, kFE）
        public String baseUnit;            // 基础单位名称（如 "FE"）
        public boolean compactMode;        // 紧凑模式
        public boolean compactUnit = false;        // 单位不在TextSwitchUnit中显示

        public NumberDisplayWidget(Coordinate pos, SyncedValue<Double> number, int digits, int scale, TextSwitchWidget unitWidget, String baseUnit, Boolean compactMode) {
            // 初始化大小：宽度 = (8 * 缩放 * 位数 - 缩放) [计算出刚好容纳所有数字的宽度]，高度 = 9 * 缩放
            super(pos, Coordinate.fromTopLeft((compactMode ? 7 : 8) * scale * digits - scale, 9 * scale));
            this.number = number;
            this.digits = digits;
            this.scale = scale;
            this.unitWidget = unitWidget;
            this.baseUnit = baseUnit;
            this.compactMode = compactMode;

            // 根据指定的位数，生成对应的单数字组件，并作为子组件添加

            for (int i = 0; i < digits; i++)
                digit.add((NumberDigitWidget) addChild(new NumberDigitWidget(
                        Coordinate.fromTopLeft(scale + (compactMode ? 6 : 8) * scale * i, scale), // 错开 X 坐标排列
                        Coordinate.fromTopLeft(40, 50), scale)));
        }

        public NumberDisplayWidget(Coordinate pos, SyncedValue<Double> number, int digits, int scale, Boolean compactMode) {
            // 初始化大小：宽度 = (8 * 缩放 * 位数 - 缩放) [计算出刚好容纳所有数字的宽度]，高度 = 9 * 缩放
            super(pos, Coordinate.fromTopLeft((compactMode ? 7 : 8) * scale * digits - scale, 9 * scale));
            this.number = number;
            this.digits = digits;
            this.scale = scale;
            this.compactMode = compactMode;
            this.compactUnit = true;

            // 根据指定的位数，生成对应的单数字组件，并作为子组件添加

            for (int i = 0; i < digits; i++)
                digit.add((NumberDigitWidget) addChild(new NumberDigitWidget(
                        Coordinate.fromTopLeft(scale + (compactMode ? 6 : 8) * scale * i, scale), // 错开 X 坐标排列
                        Coordinate.fromTopLeft(40, 50), scale)));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick, double dt, boolean isForeground) {
            // 开启 OpenGL 裁剪：由于子组件（数字）在滚动时会超出自身框的范围
            // 这里设置裁剪区，确保只有在当前组件区域内的像素才会被渲染出，达到“遮罩”效果
            graphics.enableScissor(left(), top(), right(), bottom());
            super.renderWidget(graphics, mx, my, partialTick, dt, isForeground);
            // 渲染完毕后关闭裁剪，以免影响其他 GUI 元素的渲染
            graphics.disableScissor();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // 为每一个数字槽位绘制背景色块
            if (!compactMode) for (int i = 0; i < digits; i++)
                graphics.fill(left() + 8 * i * scale, top(), left() + 8 * i * scale + 7 * scale, bottom(), bgColorInt());
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
            for (int i = 0; i < digits; i++) {
                digit.get(i).setChar((!compactUnit ? mantissa : formatted).charAt(i));
            }
        }

        /**
         * 负责处理单个字符（0-9, 小数点, 空格）渲染和滚动动画的内部类
         */
        public static class NumberDigitWidget extends Widget implements Renderable {
            public int digit = 0, scale;
            public char old = ' ';        // 切换动画时的旧字符
            public char current = ' ';    // 当前字符
            public SmoothedValue textDy = new SmoothedValue(0); // 用于非数字字符切换时的垂直平滑位移
            public boolean isSwitching = false; // 是否正在进行非数字的切换动画

            public NumberDigitWidget(Coordinate pos, Coordinate size, int scale) {
                super(pos, size);
                this.scale = scale;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                int lineHeight = scale * 10; // 每个字符占据的高度
                // dy 是父类 Widget 里的变量，这里被用来表示滚动纸带的总偏移量
                int baseY = menuY() - dy.getInt();
                // 状态1：正在进行非数字切换动画（例如 空格 -> 1, 或者 5 -> .）
                if (isSwitching) {
                    textDy.doStep(screen.dt); // 更新平滑动画步长

                    // 绘制旧字符（逐渐滑出）
                    if (old != ' ')
                        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(old)), menuX(), baseY - lineHeight + textDy.getInt(), scale, mainColorInt(), false);

                    // 绘制新字符（逐渐滑入）
                    if (current != ' ')
                        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(current)), menuX(), baseY + textDy.getInt(), scale, mainColorInt(), false);

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
                        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(current)), menuX(), baseY, scale, mainColorInt(), false);
                    return;
                }
                // 状态3：当前字符是数字 0-9，执行数字卷轴渲染逻辑
                // 绘制一列 0 到 9，再加上一个 0 （总共11个），方便 9 向下无缝滚动到 0
                for (int i = 0; i < 11; i++)
                    TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(String.valueOf(i % 10)), menuX(), menuY() + lineHeight * i, scale, mainColorInt(), false);
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
                int lineHeight = scale * 10; // 每个字符占据的高度

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
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
        public SyncedValue<Double> result;
        public Component resultUnit, description;
        public Widget descWidget;
        public boolean isHovering, isExpanded = false;
        public List<DetailLineWidget> detailLines = new ArrayList<>();

        public CalcationDetailsWidget(Coordinate pos, Coordinate size, SyncedValue<Double> result, Component resultUnit, Component description) {
            super(pos, size);

            this.result = result;
            this.resultUnit = resultUnit;
            this.description = description;
        }

        @Override
        public void onInitialize() {

            // 计算结果
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(1, 1), result, 5, 1, true));
            // 计算结果单位
            addChild(new TextWidget(Coordinate.fromTopLeft(w() - 1, 2), resultUnit).rightAlign().noShadow());
            // 描述
            descWidget = addChild(new RectangleWidget(Coordinate.fromTopLeft(0, -2), Coordinate.fromTopLeft(w(), 1)));
            descWidget.addChild(new TextWidget(Coordinate.fromTopLeft(w() / 2, -9), description).centerAlign());
            descWidget.disable();
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
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(left(), top(), right(), bottom(), bgColorInt());

            // 悬浮动画检测
            var isInBound = isInBounds(mouseX, mouseY);
            if (!isHovering && isInBound && !isExpanded) descWidget.addAnimation(new Animation.FadeIn.FromBottom(0.2), 0);
            else if (!isInBound && isHovering && !isExpanded) descWidget.addAnimation(new Animation.FadeOut.ToTop(0.2).noDeletion(), 0);

            descWidget.dy.set(isExpanded ? -(h() + 1) * detailLines.size() : 0);

            isHovering = isInBound;
        }

        public void addDetailLine(Component desc, SyncedValue<Double> number, Component tooltip) {
            var line = addChild(new DetailLineWidget(Coordinate.fromTopLeft(0, (detailLines.size() + 1) * -(h() + 1)), originalSize, desc, number, bgColor(), mainColor()));
            line.disable().tooltip(tooltip);
            detailLines.add((DetailLineWidget) line);
        }

        public static class DetailLineWidget extends Widget {
            public DetailLineWidget(Coordinate pos, Coordinate size, Component desc, SyncedValue<Double> number, Color bgColor, Color mainColor) {
                super(pos, size);

                addChild(new RectangleWidget(Coordinate.ZERO, size).mainColor(bgColor));
                addChild(new TextWidget(Coordinate.fromTopLeft(3, 1), desc).noShadow().mainColor(mainColor));
                addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(size.toScreenX() - 32, 1), number, 5, 1, true));
            }
        }
    }
}
