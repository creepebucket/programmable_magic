package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class ProgressBarWidget extends Widget implements Renderable, Lifecycle {
    public DynamicValue<Double> numerator, denominator;
    public SmoothedValue smoothed = new SmoothedValue(0);
    public NumberDisplayWidget ratioWidget;
    public TextWidget textWidget;
    public double lastFillX = 0;

    public ProgressBarWidget(Coordinate pos, Coordinate size, DynamicValue<Double> numerator, DynamicValue<Double> denominator) {
        super(pos, size);

        this.numerator = numerator;
        this.denominator = denominator;

        smoothedValues.add(smoothed);
    }

    @Override
    public void onInitialize() {
        textWidget = new TextWidget(Coordinate.fromCenterLeft(-2, 1), Component.literal("%"));
        addChild(textWidget.noShadow().rightAlign().centerAlignY());

        ratioWidget = new NumberDisplayWidget(Coordinate.fromCenterLeft(-9, 0), DynamicValue.fromSupplier(() -> numerator.get() * 100 / denominator.get()), 5, 1, true);
        addChild(ratioWidget.rightAlign().centerAlignY());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        smoothed.set(numerator.get());
        var fillX = w() * smoothed.get() / denominator.get();

        var filled = (int) (left() + Math.round(fillX));
        graphics.fill(left(), top(), filled, bottom(), mainColorInt());
        graphics.fill(filled, top(), right(), bottom(), bgColorInt());

        textWidget.dx.set(fillX < 38 ? fillX + 39 : fillX);
        ratioWidget.dx.set(fillX < 38 ? fillX + 39 : fillX);

        if (fillX < 38) {
            textWidget.mainColor(mainColor().toArgbWithAlphaMult(0.2));
            ratioWidget.mainColor(mainColor().toArgbWithAlphaMult(0.2));
        } else {
            textWidget.mainColor(0x9f000000);
            ratioWidget.mainColor(0x9f000000);
        }

        if (h() >= 9) {
            textWidget.enable();
            ratioWidget.enable();
        } else {
            textWidget.disable();
            ratioWidget.disable();
        }
    }
}
