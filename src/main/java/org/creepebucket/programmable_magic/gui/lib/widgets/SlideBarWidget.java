package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.input.MouseButtonEvent;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseDraggable;

public class SlideBarWidget extends Widget implements MouseDraggable, Clickable, Lifecycle {
    public boolean focus = true;
    public Coordinate region;
    public GridentRectangleWidget background;
    public RectangleWidget selection;
    public double value;

    public SlideBarWidget(Coordinate pos, Coordinate size, Coordinate region) {
        super(pos, size);
        this.region = region;
    }

    @Override
    public void onInitialize() {
        background = (GridentRectangleWidget) addChild(new GridentRectangleWidget(Coordinate.ZERO, originalSize).color(originalBgColor));
        selection = (RectangleWidget) addChild(new RectangleWidget(Coordinate.ZERO, Coordinate.fromTopLeft(3, h())).color(originalMainColor));
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        focus = false;
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        if (!selection.isInBounds(event.x(), event.y())) return false;
        focus = true;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!focus) return false;

        value = Math.clamp(value + dragX / w() * (region.toScreenY() - region.toScreenX()), region.toScreenX(), region.toScreenY());
        // 设置selection
        selection.dx.setImmediate((value - region.toScreenX()) / (region.toScreenY() - region.toScreenX()) * (w() - 3));

        return true;
    }
}
