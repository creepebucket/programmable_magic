package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

/**
 * 纹理控件：在指定坐标绘制一张 GUI 纹理。
 */
public class TextureWidget extends Widget implements Renderable {
    /** 纹理左上角坐标 */
    public final Coordinate pos;
    /** 纹理资源标识 */
    public final Identifier texture;
    /** 纹理宽度 */
    public final int width;
    /** 纹理高度 */
    public final int height;

    /**
     * 创建一个纹理控件。
     */
    public TextureWidget(Coordinate pos, Identifier texture, int width, int height) {
        this.pos = pos;
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    /**
     * 按当前坐标渲染纹理。
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture,
                this.pos.toMenuX(), this.pos.toMenuY(),
                0, 0,
                this.width, this.height,
                this.width, this.height);
    }
}
