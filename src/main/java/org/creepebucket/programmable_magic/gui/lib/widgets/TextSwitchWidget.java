package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class TextSwitchWidget extends Widget implements Renderable {
    public String old = "";
    public String current = "";
    public int scale;
    public SmoothedValue textDy = new SmoothedValue(0);

    public TextSwitchWidget(Coordinate pos, Coordinate size, int scale, String initial) {
        super(pos, size);

        this.current = initial;
        this.scale = scale;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        textDy.doStep(screen.dt);

        graphics.enableScissor(left(), top(), right(), bottom());

        graphics.fill(left(), top(), right(), bottom(), bgColorInt());
        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(old), left() + scale, top() + scale - h() + textDy.getInt(), scale, mainColorInt(), false);
        TextWidget.drawScaledString(graphics, ClientUiContext.getFont(), Component.literal(current), left() + scale, top() + scale + textDy.getInt(), scale, mainColorInt(), false);

        graphics.disableScissor();
    }

    public void switchText(String newText) {
        textDy.setImmediate(h());
        textDy.set(0);

        old = current;
        current = newText;
    }
}
