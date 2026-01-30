package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

/**
 * 纹理控件：在指定坐标绘制一张 GUI 纹理。
 */
public class TextureWidget extends Widget implements Renderable {
    /**
     * 纹理资源标识
     */
    public final Identifier texture;

    /**
     * 创建一个纹理控件。
     */
    public TextureWidget(Coordinate pos, Identifier texture, Coordinate size) {
        super(pos, size);
        this.texture = texture;
    }

    /**
     * 按当前坐标渲染纹理。
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture,
                this.pos.toMenuX(), this.pos.toMenuY(),
                0, 0,
                w, h,
                w, h);
    }
}
