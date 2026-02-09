package org.creepebucket.programmable_magic.gui.lib.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.KeyInputable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseDraggable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.jspecify.annotations.Nullable;

public class InputBoxWidget extends Widget implements Renderable, Clickable, KeyInputable, MouseDraggable, Lifecycle {

    public EditBox box;
    private final Component message;
    private final String initialValue;
    private final int maxLength;

    public InputBoxWidget(Coordinate pos, Coordinate size, Component message) {
        this(pos, size, message, "", 32);
    }

    public InputBoxWidget(Coordinate pos, Coordinate size, Component message, String initialValue, int maxLength) {
        super(pos, size);
        this.message = message;
        this.initialValue = initialValue;
        this.maxLength = maxLength;

        onInitialize();
    }

    @Override

    public void onInitialize() {
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();

        if (this.box == null) {
            this.box = new EditBox(ClientUiContext.getFont(), x, y, w, h, this.message);
            this.box.setMaxLength(this.maxLength);
            this.box.setValue(this.initialValue);
            this.box.setCanLoseFocus(true);
        } else {
            this.box.setRectangle(w, h, x, y);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.box.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        if (contains(event.x(), event.y())) {
            this.box.setFocused(true);
            return this.box.mouseClicked(event, fromMouse);
        }
        this.box.setFocused(false);
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return this.box.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return this.box.mouseDragged(event, dragX, dragY);
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
}

