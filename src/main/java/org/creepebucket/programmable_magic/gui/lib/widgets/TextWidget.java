package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    /**
     * 文本内容提供器
     */
    private Component text;
    public double scale = 1;
    public boolean shadow = true;

    public TextWidget(Coordinate pos, Component text) {
        super(pos, Coordinate.ZERO);
        setText(text);
    }

    public Component getText() {
        return text;
    }

    public TextWidget setText(Component text) {
        this.text = text;
        Font font = ClientUiContext.getFont();
        int w = (int) Math.round(font.width(text) * scale);
        int h = (int) Math.round(font.lineHeight * scale);
        originalSize = Coordinate.fromTopLeft(w, h);
        return this;
    }

    public TextWidget scaled(double fact) {
        this.scale = fact;
        return setText(text);
    }

    public TextWidget noShadow() {
        shadow = false;
        return this;
    }

    @Override
    public int w() {
        return originalSize.x.apply(0, 0);
    }

    @Override
    public int h() {
        return originalSize.y.apply(0, 0);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Font font = ClientUiContext.getFont();
        float scaled = (float) scale;
        float x = menuX();
        float y = menuY();
        drawScaledString(graphics, font, text, x, y, scaled, mainColorInt(), shadow);
    }

    public static void drawScaledString(GuiGraphicsExtractor guiGraphics, Font font, Component text, float x, float y, float scale, int color, boolean dropShadow) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.scale(scale, scale);
        guiGraphics.text(font, text, 0, 0, color, dropShadow);
        poseStack.popMatrix();
    }
}
