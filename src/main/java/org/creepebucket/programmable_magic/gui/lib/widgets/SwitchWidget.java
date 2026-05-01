package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

import java.util.function.Consumer;

public class SwitchWidget extends Widget implements Renderable, Clickable, Lifecycle {
    public Component text1, text2;
    public boolean pressed = false;
    public SmoothedValue rectDx = new SmoothedValue(0);
    public Consumer<Boolean> onSwitch = pressed -> {};

    public SwitchWidget(Coordinate pos, Coordinate size, Component text1, Component text2) {
        super(pos, size);

        this.text1 = text1;
        this.text2 = text2;

        smoothedValues.add(rectDx);
    }

    public SwitchWidget reverseInitialSelection() {
        setPressed(true);
        return this;
    }

    public SwitchWidget setPressed(boolean pressed) {
        if (this.pressed == pressed) return this;
        this.pressed = pressed;
        rectDx.set(pressed ? (double) w() / 2 : 0);
        return this;
    }

    public SwitchWidget onSwitch(Consumer<Boolean> onSwitch) {
        this.onSwitch = onSwitch;
        return this;
    }

    @Override
    public void onInitialize() {
        addChild(new TextWidget(Coordinate.fromTopLeft(w() / 4 + 1, h() / 2 + 1), text1).centerAlign().centerAlignY().scaled(Math.max(1, Math.floor((double) h() / 11))).mainColor(textColor()));
        addChild(new TextWidget(Coordinate.fromTopLeft(3 * w() / 4 + 1, h() / 2 + 1), text2).centerAlign().centerAlignY().scaled(Math.max(1, Math.floor((double) h() / 11))).mainColor(textColor()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(left() + rectDx.getInt(), top(), left() + w() / 2 + rectDx.getInt(), bottom(), bgColorInt());
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        setPressed(!pressed);

        onSwitch.accept(pressed);
        return true;
    }
}
