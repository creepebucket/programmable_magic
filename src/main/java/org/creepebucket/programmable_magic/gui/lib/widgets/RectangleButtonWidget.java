package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class RectangleButtonWidget extends Widget implements Renderable, Clickable {
    public Runnable onPress;

    public RectangleButtonWidget(Coordinate pos, Coordinate size, Runnable onPress) {
        super(pos, size);
        this.onPress = onPress;
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        onPress.run();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(left(), top(), right(), bottom(), isInBounds(mouseX, mouseY) ? mainColorInt() : bgColorInt());
        graphics.fill(x() + w() / 3, y() + h() / 2, x() + w() * 2 / 3, y() + h() / 2 + 1, isInBounds(mouseX, mouseY) ? bgColorInt() : mainColorInt());
    }
}
