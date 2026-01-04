package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

public class TextureWidget extends Widget {

    public final Coordinate pos;
    public final Identifier texture;
    public final int width;
    public final int height;

    public TextureWidget(Coordinate pos, Identifier texture, int width, int height) {
        this.pos = pos;
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture,
                this.pos.toScreenX(), this.pos.toScreenY(),
                0, 0,
                this.width, this.height,
                this.width, this.height);
    }
}

