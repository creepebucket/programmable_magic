package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class Widget {
    public void onInitialize() {}
    public void onRemoved() {}

    public void onResize(int width, int height) {}
    public void onTick() {}
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    public boolean mouseClicked(MouseButtonEvent event) { return false; }
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) { return false; }
    public boolean mouseReleased(MouseButtonEvent event) { return false; }
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) { return false; }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return false; }

    public boolean keyPressed(KeyEvent event) { return false; }
    public boolean keyReleased(KeyEvent event) { return false; }
    public boolean charTyped(CharacterEvent event) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean charTyped(char codePoint, int modifiers) { return false; }
}
