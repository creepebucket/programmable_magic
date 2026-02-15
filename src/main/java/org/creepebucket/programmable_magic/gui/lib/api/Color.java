package org.creepebucket.programmable_magic.gui.lib.api;

public class Color {
    public int r, g, b, a;

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
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
}
