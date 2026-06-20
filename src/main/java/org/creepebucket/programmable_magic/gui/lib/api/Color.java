package org.creepebucket.programmable_magic.gui.lib.api;

public class Color {
    public int r, g, b, a;

    // 预定义常用颜色常量（避免反复创建对象）
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0);

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(double r, double g, double b) {
        this((int) Math.round(r), (int) Math.round(g), (int) Math.round(b));
    }

    public Color(int argb) {
        this.a = (argb & 0xFF000000) >>> 24;
        this.r = (argb & 0x00FF0000) >>> 16;
        this.g = (argb & 0x0000FF00) >>> 8;
        this.b = (argb & 0x000000FF);
    }

    public int toArgb() {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public int toArgbWithAlphaMult(double mult) {
        mult = Math.max(0, Math.min(1, mult));
        return (int) (a * mult) << 24 | r << 16 | g << 8 | b;
    }

    public Color mix(Color target, double ratio) {
        return new Color(
                r * (1 - ratio) + target.r * ratio,
                g * (1 - ratio) + target.g * ratio,
                b * (1 - ratio) + target.b * ratio
        );
    }

    /**
     * 根据当前颜色的相对亮度，返回与之对比度较高的文字颜色（黑色或白色）。
     * 算法基于 WCAG 2.0 相对亮度公式，阈值 0.179 近似于 18% 灰。
     *
     * @return 适合在背景上显示的文字颜色（BLACK 或 WHITE）
     */
    public Color getTextColor() {
        // 将 sRGB 分量归一化到 [0, 1]
        double sr = r / 255.0;
        double sg = g / 255.0;
        double sb = b / 255.0;

        // sRGB 线性化
        double rLin = (sr <= 0.04045) ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
        double gLin = (sg <= 0.04045) ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
        double bLin = (sb <= 0.04045) ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

        // 计算相对亮度 Y
        double luminance = 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;

        // 亮度高于阈值用黑色文字，否则用白色文字
        return luminance > 0.179 ? BLACK : WHITE;
    }
}
