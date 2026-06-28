package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;

import java.util.ArrayList;
import java.util.List;

/**
 * 纯ai （注：可能是作者留下的注释，暗示这部分逻辑非常巧妙/自动化）
 * 整体数字显示屏组件，包含多个单数字体（Digit）和一个单位显示器
 */
public class NumberDisplayWidget extends Widget implements Renderable, Tickable {
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
        super(pos, Coordinate.fromTopLeft((int) ((compactMode ? 6 : 8) * scale * digits - scale + 1), (int) (9 * scale)));
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
        graphics.enableScissor(left(), top(), right() + 1, bottom());
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

}
