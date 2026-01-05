package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TextWidget extends Widget {
    private final Coordinate pos;
    private final Supplier<String> text;
    private final IntSupplier color;

    public TextWidget(Coordinate pos, Supplier<String> text, IntSupplier color) {
        this.pos = pos;
        this.text = text;
        this.color = color;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(Minecraft.getInstance().font, this.text.get(),
                this.pos.toMenuX(),
                this.pos.toMenuY(),
                this.color.getAsInt());
    }
}
