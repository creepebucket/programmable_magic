package org.creepebucket.programmable_magic.gui.lib.widgets;

import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ScrollRegionWidget extends Widget {
    private final Coordinate pos;
    private final IntSupplier width;
    private final IntSupplier height;
    private final IntSupplier getValue;
    private final IntSupplier maxValue;
    private final IntConsumer setValue;

    public ScrollRegionWidget(Coordinate pos, IntSupplier width, IntSupplier height, IntSupplier getValue, IntSupplier maxValue, IntConsumer setValue) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.getValue = getValue;
        this.maxValue = maxValue;
        this.setValue = setValue;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        int w = this.width.getAsInt();
        int h = this.height.getAsInt();
        if (mouseX < x || mouseX >= x + w || mouseY < y || mouseY >= y + h) return false;
        int d = scrollY > 0 ? -1 : (scrollY < 0 ? 1 : 0);
        int cur = this.getValue.getAsInt();
        int next = cur + d;
        int max = Math.max(0, this.maxValue.getAsInt());
        if (next < 0) next = 0;
        if (next > max) next = max;
        if (next == cur) return true;
        this.setValue.accept(next);
        return true;
    }
}
