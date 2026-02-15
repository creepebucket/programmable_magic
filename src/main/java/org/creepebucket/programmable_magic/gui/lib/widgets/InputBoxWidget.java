package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.*;
import org.jspecify.annotations.Nullable;

public class InputBoxWidget extends Widget implements Renderable, Clickable, KeyInputable, MouseDraggable, Lifecycle {

    private final String initialValue;
    private final int maxLength;
    public CustomizableEditBox box;
    public double widthExtend = 0, xExtend = 0;

    public InputBoxWidget(Coordinate pos, Coordinate size, String initialValue, int maxLength) {
        super(pos, size);
        this.initialValue = initialValue;
        this.maxLength = maxLength;
    }

    public InputBoxWidget extendWhenFocus(double widthExtend, double xExtend) {
        this.widthExtend = widthExtend;
        this.xExtend = xExtend;
        return this;
    }

    @Override
    public void onInitialize() {

        if (this.box == null) {
            this.box = new CustomizableEditBox(ClientUiContext.getFont(), x(), y(), w() - 6, h(), Component.empty(), textColor());
            this.box.setMaxLength(this.maxLength);
            this.box.setValue(this.initialValue);
            this.box.setCanLoseFocus(true);
        } else {
            this.box.setRectangle(w() - 6, h(), x(), y());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(x(), y(), x() + w(), y() + h(), bgColor());
        graphics.renderOutline(x(), y(), w(), h(), mainColor());

        this.box.setRectangle(w() - 6, h(), x(), y());
        this.box.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        if (isInBounds(event.x(), event.y())) {
            this.box.setFocused(true);
            dx.set(xExtend);
            dw.set(widthExtend);
            return this.box.mouseClicked(event, fromMouse);
        }
        this.box.setFocused(false);
        dx.set(0);
        dw.set(0);
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (box.isFocused()) return this.box.mouseReleased(event);
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isInBounds(event.x(), event.y())) return this.box.mouseDragged(event, dragX, dragY);
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.box.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return this.box.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return this.box.charTyped(event);
    }

    public static class CustomizableEditBox extends EditBox {

        public CustomizableEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message, int mainColor) {
            super(font, x, y, width, height, editBox, message);

            this.setTextColor(mainColor);
        }

        public CustomizableEditBox(Font font, int x, int y, int width, int height, Component message, int mainColor) {
            super(font, x, y, width, height, message);

            this.setTextColor(mainColor);
        }

        public CustomizableEditBox(Font font, int width, int height, Component message, int mainColor) {
            super(font, width, height, message);

            this.setTextColor(mainColor);
        }

        @Override
        public boolean isBordered() {
            return false;
        }
    }
}

