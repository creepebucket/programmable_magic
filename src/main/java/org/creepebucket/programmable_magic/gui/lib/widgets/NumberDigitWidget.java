package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

/**
 * 负责处理单个字符（0-9, 小数点, 空格）渲染和滚动动画的内部类
 */
public class NumberDigitWidget extends Widget implements Renderable {
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
