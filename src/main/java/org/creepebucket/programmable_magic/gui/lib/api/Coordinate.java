package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.function.BiFunction;

/**
 * 坐标计算器：以当前窗口缩放后的尺寸为输入，计算屏幕坐标与菜单坐标。
 */
public class Coordinate {

    /**
     * 以 (sw, sh) 为输入的 X 计算函数。
     */
    public final BiFunction<Integer, Integer, Integer> x;

    /**
     * 以 (sw, sh) 为输入的 Y 计算函数。
     */
    public final BiFunction<Integer, Integer, Integer> y;

    /**
     * 构造时捕获的窗口宽（缩放后）。
     */
    public final int sw;

    /**
     * 构造时捕获的窗口高（缩放后）。
     */
    public final int sh;

    /**
     * 创建一个坐标计算器，并捕获当前窗口缩放后的尺寸。
     */
    public Coordinate(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {
        this.x = x;
        this.y = y;

        var window = Minecraft.getInstance().getWindow();
        this.sw = window.getGuiScaledWidth();
        this.sh = window.getGuiScaledHeight();
    }

    /**
     * 将屏幕坐标转换为当前菜单坐标（要求当前 screen 为 {@link AbstractContainerScreen}）。
     */
    public int toMenuX() {
        return toScreenX() - ((AbstractContainerScreen<?>) Minecraft.getInstance().screen).getGuiLeft();
    }

    /**
     * 将屏幕坐标转换为当前菜单坐标（要求当前 screen 为 {@link AbstractContainerScreen}）。
     */
    public int toMenuY() {
        return toScreenY() - ((AbstractContainerScreen<?>) Minecraft.getInstance().screen).getGuiTop();
    }

    /**
     * 计算屏幕坐标 X。
     */
    public int toScreenX() { return this.x.apply(this.sw, this.sh); }

    /**
     * 计算屏幕坐标 Y。
     */
    public int toScreenY() { return this.y.apply(this.sw, this.sh); }

    /**
     * 以屏幕左上角为基准创建坐标。
     */
    public static Coordinate fromTopLeft(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> deltaX, (sw, sh) -> deltaY);
    }

    /**
     * 以屏幕左下角为基准创建坐标。
     */
    public static Coordinate fromBottomLeft(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> deltaX, (sw, sh) -> sh - deltaY);
    }

    /**
     * 以屏幕右上角为基准创建坐标。
     */
    public static Coordinate fromTopRight(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> sw - deltaX, (sw, sh) -> deltaY);
    }

    /**
     * 以屏幕右下角为基准创建坐标。
     */
    public static Coordinate fromBottomRight(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> sw - deltaX, (sw, sh) -> sh - deltaY);
    }

    /**
     * 以屏幕中心为基准创建坐标。
     */
    public static Coordinate fromCenter(int deltaX, int deltaY) {
        return new Coordinate((sw, sh) -> sw / 2 + deltaX, (sw, sh) -> sh / 2 + deltaY);
    }
}
