package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class StringButtonWidget extends Widget implements Renderable, Clickable, Lifecycle {
    public Runnable onPress;
    public Component text;

    public StringButtonWidget(Coordinate pos, Coordinate size, Component text, Runnable onPress) {
        super(pos, size);

        this.text = text;
        this.onPress = onPress;
    }

    @Override
    public void onInitialize() {
        addChild(new TextWidget(Coordinate.fromTopLeft(w() / 2 - ClientUiContext.getFont().width(text) / 2, h() / 2 - 4), text).color(originalTextColor));
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        onPress.run();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(left(), top(), right(), bottom(), isInBounds(mouseX, mouseY) ? mainColor() : bgColor());
    }
}
