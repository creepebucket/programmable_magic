package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 文本控件：在指定坐标渲染动态文本。
 */
public class TextWidget extends Widget implements Renderable {
    /** 文本内容提供器 */
    private final Supplier<String> text;
    /** 文本颜色提供器 */
    private final IntSupplier color;

    public TextWidget(Coordinate pos, Supplier<String> text, IntSupplier color) {
        super(pos);
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(Minecraft.getInstance().font, this.text.get(),
                this.pos.toMenuX(),
                this.pos.toMenuY(),
                this.color.getAsInt());
    }
}
