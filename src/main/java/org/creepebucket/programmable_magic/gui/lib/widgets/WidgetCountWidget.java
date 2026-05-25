package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class WidgetCountWidget extends Widget implements Renderable, Lifecycle {
    TextWidget text = null;

    public WidgetCountWidget(Coordinate pos, Coordinate size) {
        super(pos, size);
    }

    @Override
    public void onInitialize() {
        text = new TextWidget(originalPos, Component.literal(" "));
        addChild(text);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Widget p = this;
        Widget last = null;

        while (p != null) {
            last = p;
            p = p.parent;
        }

        var a = last.allChild().size();

        text.setText(Component.literal("Widgets: " + last.allChild().size()));
    }
}
