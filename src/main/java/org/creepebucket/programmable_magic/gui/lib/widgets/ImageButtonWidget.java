package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.Consumer;

public class ImageButtonWidget extends Widget {
    private final Coordinate pos;
    private final int width;
    private final int height;
    private final Identifier normal;
    private final Identifier hover;
    private final Consumer<MouseButtonEvent> onPress;

    public ImageButtonWidget(Coordinate pos, int width, int height, Identifier normal, Identifier hover, Runnable onPress) {
        this(pos, width, height, normal, hover, e -> onPress.run());
    }

    public ImageButtonWidget(Coordinate pos, int width, int height, Identifier normal, Identifier hover, Consumer<MouseButtonEvent> onPress) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.normal = normal;
        this.hover = hover;
        this.onPress = onPress;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int sx = this.pos.toScreenX();
        int sy = this.pos.toScreenY();
        boolean hovered = mouseX >= sx && mouseX < sx + this.width && mouseY >= sy && mouseY < sy + this.height;
        var tex = hovered ? this.hover : this.normal;
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.pos.toMenuX(), this.pos.toMenuY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        double mx = event.x();
        double my = event.y();
        if (mx < x || mx >= x + this.width || my < y || my >= y + this.height) return false;
        this.onPress.accept(event);
        return true;
    }
}
