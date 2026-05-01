package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class ProgressBarWidget extends Widget implements Renderable {
    public SyncedValue<Double> current, max;
    public SmoothedValue smoothed = new SmoothedValue(0);

    public ProgressBarWidget(Coordinate pos, Coordinate size, SyncedValue<Double> value, SyncedValue<Double> max) {
        super(pos, size);

        this.current = value;
        this.max = max;

        smoothedValues.add(smoothed);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        smoothed.set(current.get());
        var filled = (int) (left() + Math.round(w() * smoothed.get() / max.get()));
        graphics.fill(left(), top(), filled, bottom(), mainColorInt());
        graphics.fill(filled, top(), right(), bottom(), bgColorInt());
    }
}
