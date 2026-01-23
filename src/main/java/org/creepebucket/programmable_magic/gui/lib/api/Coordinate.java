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
     * 创建一个坐标计算器。
     */
    public Coordinate(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {
        this.x = x;
        this.y = y;
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
    public int toScreenX() {
        var window = Minecraft.getInstance().getWindow();
        return this.x.apply(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    /**
     * 计算屏幕坐标 Y。
     */
    public int toScreenY() {
        var window = Minecraft.getInstance().getWindow();
        return this.y.apply(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    /**
     * 一次性计算屏幕坐标 [x, y]，避免重复获取 window。
     */
    public int[] toScreen() {
        var window = Minecraft.getInstance().getWindow();
        int sw = window.getGuiScaledWidth();
        int sh = window.getGuiScaledHeight();
        return new int[]{this.x.apply(sw, sh), this.y.apply(sw, sh)};
    }

    /**
     * 一次性计算菜单坐标 [x, y]，避免重复获取 window 和 screen。
     */
    public int[] toMenu() {
        var window = Minecraft.getInstance().getWindow();
        var screen = (AbstractContainerScreen<?>) Minecraft.getInstance().screen;
        int sw = window.getGuiScaledWidth();
        int sh = window.getGuiScaledHeight();
        return new int[]{this.x.apply(sw, sh) - screen.getGuiLeft(), this.y.apply(sw, sh) - screen.getGuiTop()};
    }

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
