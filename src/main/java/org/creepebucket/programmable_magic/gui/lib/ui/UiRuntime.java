package org.creepebucket.programmable_magic.gui.lib.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.creepebucket.programmable_magic.gui.lib.api.DataInstance;
import org.creepebucket.programmable_magic.gui.lib.api.DataManager;
import org.creepebucket.programmable_magic.gui.lib.api.DataType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class UiRuntime {

    private final DataManager data = new DataManager();
    private final List<Widget> widgets = new ArrayList<>();

    private UiBounds bounds = new UiBounds(0, 0, 0, 0);

    public DataInstance data(String key, DataType type, SyncMode syncMode, Object initialValue) {
        return this.data.request(key, type, syncMode, initialValue);
    }

    public void bindSendToServer(BiConsumer<String, Object> sender) { this.data.bindSendToServer(sender); }
    public void bindSendToClient(BiConsumer<String, Object> sender) { this.data.bindSendToClient(sender); }

    public UiBounds bounds() { return this.bounds; }
    public void setBounds(int sw, int sh, int guiLeft, int guiTop) { this.bounds = new UiBounds(sw, sh, guiLeft, guiTop); }

    public void flushPullRequests() { this.data.flushPullRequests(); }

    public boolean handleC2S(String key, Object value) { return this.data.handleC2S(key, value); }
    public boolean handleS2C(String key, Object value) { return this.data.handleS2C(key, value); }

    public void addWidget(Widget widget) {
        this.widgets.add(widget);
        widget.onInitialize();
    }

    public void removeWidget(Widget widget) {
        this.widgets.remove(widget);
        widget.onRemoved();
    }

    public void tick() {
        for (var w : this.widgets) w.onTick();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (var w : this.widgets) w.onRender(graphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        for (var w : this.widgets) if (w.mouseClicked(event, fromMouse)) return true;
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        for (var w : this.widgets) if (w.mouseReleased(event)) return true;
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (var w : this.widgets) if (w.mouseDragged(event, dragX, dragY)) return true;
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (var w : this.widgets) if (w.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        for (var w : this.widgets) if (w.keyPressed(event)) return true;
        return false;
    }

    public boolean keyReleased(KeyEvent event) {
        for (var w : this.widgets) if (w.keyReleased(event)) return true;
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        for (var w : this.widgets) if (w.charTyped(event)) return true;
        return false;
    }
}
