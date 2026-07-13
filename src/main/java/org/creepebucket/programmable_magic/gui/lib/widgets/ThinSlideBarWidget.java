package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.input.MouseButtonEvent;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseDraggable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;


import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.*;

public class ThinSlideBarWidget extends Widget implements Lifecycle, Clickable, MouseDraggable, Tickable {
    public double min, max, step = 1, preciseValue;
    public DynamicValue<Double> value;
    public boolean focus;
    public SmoothedValue filled;

    public ThinSlideBarWidget(Coordinate pos, Coordinate size, double min, double max, DynamicValue<Double> value) {
        super(pos, size);

        this.min = min;
        this.max = max;
        this.value = value;
    }

    public ThinSlideBarWidget step(double step) {this.step = step; return this;}

    @Override
    public void onInitialize() {
        filled = new SmoothedValue(value.get() * (w() - 2) / max);
        value.whenFirstDataArrivesDo(() -> preciseValue = value.get());

        addChild(new RectangleWidget(fromCenterRight(0, 0), fromTopRight(0, 1)).dw(filled.multiply(-1).minus(3)).rightAlign().mainColor(bgColor()));
        addChild(new RectangleWidget(fromCenterLeft(0, 0), fromTopLeft(0, 1)).dw(filled));
        addChild(new OutlineWidget(fromCenterLeft(0, -1), fromTopLeft(3, 3)).dx(filled));
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        focus = true;
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        focus = false;
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (focus) {
            preciseValue += dragX * max / (w() - 2);
            value.set(Math.clamp(preciseValue - preciseValue % step, min, max));

            filled.set(value.get() * (w() - 2) / max);
        }
        return false;
    }

    @Override
    public void tick() {
        filled.set(value.get() * (w() - 2) / max);
    }
}
