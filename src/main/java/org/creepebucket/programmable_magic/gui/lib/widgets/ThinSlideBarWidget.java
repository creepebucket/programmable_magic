package org.creepebucket.programmable_magic.gui.lib.widgets;

import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;

public class SlideBarWidget extends Widget implements Lifecycle {
    public double min, max, step = 1;
    public DynamicValue<Double> value;

    public SlideBarWidget(Coordinate pos, Coordinate size, double min, double max, DynamicValue<Double> value) {
        super(pos, size);

        this.min = min;
        this.max = max;
        this.value = value;
    }

    public SlideBarWidget step(double step) {this.step = step; return this;}

    @Override
    public void onInitialize() {
        var filled = new SmoothedValue(value.get() * (w() - 2) / max);

        addChild(new RectangleWidget(Coordinate.fromCenterLeft(0, 0), Coordinate.fromTopLeft(0, 1)).dw(filled))
    }
}
