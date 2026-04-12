package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;

public class TextButtonWidget extends RectangleButtonWidget implements Lifecycle {
    public Component text;
    public SmoothedValue pivot = new SmoothedValue(0);
    public Widget textWidget;

    public TextButtonWidget(Coordinate pos, Coordinate size, Component text, Runnable onPress) {
        super(pos, size, onPress);

        this.text = text;
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        onPress.run();
        return true;
    }

    @Override
    public void onInitialize() {
        textWidget = addChild(new TextWidget(Coordinate.fromTopLeft(w() / 2 - ClientUiContext.getFont().width(text) / 2, h() / 2 - 4), text));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 悬浮逻辑
        // 悬浮状态改变按钮在"悬浮"和"正常"状态切换的程度
        if (isInBounds(mouseX, mouseY)) pivot.set(1);
        else pivot.set(0);

        graphics.fill(left(), top(), left() + (int) (w() * pivot.get()), bottom(), mainColorInt());
        graphics.fill(left() + (int) (w() * pivot.get()), top(), right(), bottom(), bgColorInt());

        textWidget.mainColor(textColor().mix(bgColor(), pivot.get())).bgColor(bgColor());

        pivot.doStep(screen.dt);
    }
}
