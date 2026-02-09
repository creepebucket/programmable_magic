package org.creepebucket.programmable_magic.gui.lib.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.KeyInputable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseDraggable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InputBoxWidget extends Widget implements Renderable, Clickable, KeyInputable, MouseDraggable, Lifecycle {

    public CustomizableEditBox box;
    private final String initialValue;
    private final int maxLength;
    public int mainColor, borderColor, backgroundColor;

    public InputBoxWidget(Coordinate pos, Coordinate size, String initialValue, int maxLength, int mainColor, int borderColor, int backgroundColor) {
        super(pos, size);
        this.initialValue = initialValue;
        this.maxLength = maxLength;

        this.backgroundColor = backgroundColor;
        this.mainColor = mainColor;
        this.borderColor = borderColor;

        onInitialize();
    }

    @Override
    public void onInitialize() {
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();

        if (this.box == null) {
            this.box = new CustomizableEditBox(ClientUiContext.getFont(), x, y, w - 6, h, Component.empty(), mainColor);
            this.box.setMaxLength(this.maxLength);
            this.box.setValue(this.initialValue);
            this.box.setCanLoseFocus(true);
        } else {
            this.box.setRectangle(w -6, h, x, y);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(pos.toScreenX(), pos.toScreenY(), pos.toScreenX() + size.toScreenX(), pos.toScreenY() + size.toScreenY(), backgroundColor);
        graphics.renderOutline(pos.toScreenX(), pos.toScreenY(), size.toScreenX(), size.toScreenY(), borderColor);

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
        if (box.isFocused()) return this.box.mouseReleased(event);
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (contains(event.x(), event.y())) return this.box.mouseDragged(event, dragX, dragY);
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

