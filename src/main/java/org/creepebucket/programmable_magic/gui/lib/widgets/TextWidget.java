package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 文本控件：在指定坐标渲染动态文本。
 */
public class TextWidget extends Widget implements Renderable {
    /**
     * 文本内容提供器
     */
    private final Component text;
    /**
     * 文本颜色提供器
     */
    private final int color;

    public TextWidget(Coordinate pos, Component text, int color) {
        super(pos);
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(ClientUiContext.getFont(), this.text, this.pos.toMenuX(), this.pos.toMenuY(), this.color);
    }
}
