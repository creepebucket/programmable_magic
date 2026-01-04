package org.creepebucket.programmable_magic.gui.lib.ui;

/**
 * UI 计算与布局所需的边界信息。
 *
 * @param sw      屏幕宽（缩放后）
 * @param sh      屏幕高（缩放后）
 * @param guiLeft GUI 左上角 X（相对屏幕）
 * @param guiTop  GUI 左上角 Y（相对屏幕）
 */
public record UiBounds(int sw, int sh, int guiLeft, int guiTop) {
}
