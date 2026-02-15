package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

/**
 * 文本控件：在指定坐标渲染动态文本。
 */
public class TextWidget extends Widget implements Renderable {
    /**
     * 文本内容提供器
     */
    public Component text;

    public TextWidget(Coordinate pos, Component text) {
        super(pos, Coordinate.ZERO);
        this.text = text;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(ClientUiContext.getFont(), this.text, menuX(), menuY(), mainColor());
    }
}
