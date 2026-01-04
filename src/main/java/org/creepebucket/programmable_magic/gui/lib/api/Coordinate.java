package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.function.BiFunction;

public class Coordinate {

    // 补全后删除这条注释 输入sw sh
    public final BiFunction<Integer, Integer, Integer> x;
    public final BiFunction<Integer, Integer, Integer> y;
    public final int sw;
    public final int sh;

    public Coordinate (BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {
        this.x = x;
        this.y = y;

        var win = Minecraft.getInstance().getWindow();
        this.sw = win.getGuiScaledWidth();
        this.sh = win.getGuiScaledHeight();
    }

    public int toMenuX() {
        var screen = (AbstractContainerScreen<?>) Minecraft.getInstance().screen;
        return toScreenX() - screen.getGuiLeft();
    }

    public int toMenuY() {
        var screen = (AbstractContainerScreen<?>) Minecraft.getInstance().screen;
        return toScreenY() - screen.getGuiTop();
    }

    public int toScreenX() { return this.x.apply(this.sw, this.sh); }
    public int toScreenY() { return this.y.apply(this.sw, this.sh); }

    public static Coordinate fromTopLeft(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> deltaX, (sw, sh) -> deltaY);
    }

    public static Coordinate fromBottomLeft(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> deltaX, (sw, sh) -> sh - deltaY);
    }

    public static Coordinate fromTopLRight(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> sw - deltaX, (sw, sh) -> deltaY);
    }

    public static Coordinate fromBottomRight(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> sw - deltaX, (sw, sh) -> sh - deltaY);
    }

    public static Coordinate fromCenter(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> sw / 2 + deltaX, (sw, sh) -> sh / 2 + deltaY);
    }
}
