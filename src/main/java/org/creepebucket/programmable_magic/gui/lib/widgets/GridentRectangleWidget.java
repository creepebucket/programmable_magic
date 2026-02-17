package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class GridentRectangleWidget extends Widget implements Renderable {
    public boolean vertical;
    public Color from, to;

    public GridentRectangleWidget(Coordinate pos, Coordinate size) {
        super(pos, size);
        this.from = originalMainColor;
        this.to = originalMainColor;
    }

    public GridentRectangleWidget color(Color from, Color to) {
        this.from = from;
        this.to = to;
        return this;
    }

    public GridentRectangleWidget vertical() {
        this.vertical = true;
        return this;
    }

    public GridentRectangleWidget swapColor() {
        var a = from;
        this.from = to;
        this.to = a;
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        double mult = 1;
        Widget parent = this;
        while (!(parent instanceof Widget.Root)) {
            for (Animation animation : parent.animations) mult *= animation.isActive() ? animation.alphaMultMain : 1;
            parent = parent.parent;
        }

        int fromArgb = this.from.toArgbWithAlphaMult(mult);
        int toArgb = this.to.toArgbWithAlphaMult(mult);

        if (vertical) {
            graphics.fillGradient(left(), top(), right(), bottom(), fromArgb, toArgb);
            return;
        }

        int x = x();
        int y = y();
        int width = w();
        int height = h();
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y + height);
        pose.rotate((float) (-Math.PI / 2));
        graphics.fillGradient(0, 0, height, width, fromArgb, toArgb);
        pose.popMatrix();
    }
}
