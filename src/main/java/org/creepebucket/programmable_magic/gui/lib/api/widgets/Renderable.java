package org.creepebucket.programmable_magic.gui.lib.api.widgets;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 渲染接口：实现此接口的 Widget 可以被渲染。
 */
public interface Renderable {
    /**
     * 渲染控件。
     */
    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
}
