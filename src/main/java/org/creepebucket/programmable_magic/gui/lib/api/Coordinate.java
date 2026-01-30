package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.function.BiFunction;

/**
 * 坐标计算器：以当前窗口缩放后的尺寸为输入，计算屏幕坐标与菜单坐标。
 *
 * @param x 以 (sw, sh) 为输入的 X 计算函数。
 * @param y 以 (sw, sh) 为输入的 Y 计算函数。
 */
public record Coordinate(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {

    private static int screenWidth;
    private static int screenHeight;
    private static int guiLeft;
    private static int guiTop;

    /**
     * 创建一个坐标计算器。
     */
    public Coordinate {
    }

    public static void updateContext(int screenWidth, int screenHeight, int guiLeft, int guiTop) {
        Coordinate.screenWidth = screenWidth;
        Coordinate.screenHeight = screenHeight;
        Coordinate.guiLeft = guiLeft;
        Coordinate.guiTop = guiTop;
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

    /**
     * 将屏幕坐标转换为当前菜单坐标（要求当前 screen 为 {@link AbstractContainerScreen}）。
     */
    public int toMenuX() {
        return toScreenX() - guiLeft;
    }

    /**
     * 将屏幕坐标转换为当前菜单坐标（要求当前 screen 为 {@link AbstractContainerScreen}）。
     */
    public int toMenuY() {
        return toScreenY() - guiTop;
    }

    /**
     * 计算屏幕坐标 X。
     */
    public int toScreenX() {
        return this.x.apply(screenWidth, screenHeight);
    }

    /**
     * 计算屏幕坐标 Y。
     */
    public int toScreenY() {
        return this.y.apply(screenWidth, screenHeight);
    }

    /**
     * 一次性计算屏幕坐标 [x, y]，避免重复获取 window。
     */
    public int[] toScreen() {
        return new int[]{this.x.apply(screenWidth, screenHeight), this.y.apply(screenWidth, screenHeight)};
    }

    /**
     * 一次性计算菜单坐标 [x, y]，避免重复获取 window 和 screen。
     */
    public int[] toMenu() {
        return new int[]{this.x.apply(screenWidth, screenHeight) - guiLeft, this.y.apply(screenWidth, screenHeight) - guiTop};
    }

    /**
     * 计算方法
     */

    public Coordinate add(Coordinate delta) {
        return new Coordinate((sw, sh) -> this.x.apply(sw, sh) + delta.x.apply(sw, sh),
                (sw, sh) -> this.y.apply(sw, sh) + delta.y.apply(sw, sh));
    }
}
