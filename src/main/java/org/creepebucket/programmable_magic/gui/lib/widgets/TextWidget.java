package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.joml.Matrix3x2fStack;

/**
 * 文本控件：在指定坐标渲染动态文本。
 */
public class TextWidget extends Widget implements Renderable {
    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * 文本内容提供器
     */
    public Component text;
    public double scale = 1;
    public boolean shadow = true;
    public Align align = Align.LEFT;

    public TextWidget(Coordinate pos, Component text) {
        super(pos, Coordinate.ZERO);
        this.text = text;
    }

    public TextWidget scaled(double fact) {
        this.scale = fact;
        return this;
    }

    public TextWidget noShadow() {
        shadow = false;
        return this;
    }

    public TextWidget rightAlign() {
        align = Align.RIGHT;
        return this;
    }

    public TextWidget centerAlign() {
        align = Align.CENTER;
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = ClientUiContext.getFont();
        float scaled = (float) scale;
        float x = menuX();
        if (align == Align.CENTER) {
            x -= font.width(text) * scaled / 2;
        } else if (align == Align.RIGHT) {
            x -= font.width(text) * scaled;
        }
        drawScaledString(graphics, font, text, x, menuY(), scaled, mainColorInt(), shadow);
    }

    public static void drawScaledString(GuiGraphics guiGraphics, Font font, Component text, float x, float y, float scale, int color, boolean dropShadow) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.scale(scale, scale);
        guiGraphics.drawString(font, text, 0, 0, color, dropShadow);
        poseStack.popMatrix();
    }
}
