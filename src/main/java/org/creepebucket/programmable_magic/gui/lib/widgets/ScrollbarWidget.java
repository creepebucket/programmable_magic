package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseDraggable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class ScrollbarWidget extends Widget implements MouseDraggable, Clickable, Renderable {

    public int minValue, maxValue, color;
    public SyncedValue<Integer> value;
    public boolean isDragging = false, reverseDirection;
    public String axis;
    public Coordinate size, pos;

    public ScrollbarWidget(Coordinate pos, Coordinate size, int minValue, int maxValue, SyncedValue<Integer> value, int color, String axis, boolean reverseDirection) {
        this.pos = pos;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.color = color;
        this.axis = axis;
        this.size = size;
        this.value = value;
        this.reverseDirection = reverseDirection;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!isDragging) return false;

        // 决定缩放因子和值修改
        // h/w -> max 映射

        this.width = size.toMenuX();
        this.height = size.toMenuY();

        int screenValue;
        if(axis == "x" || axis == "X") screenValue = width; else screenValue = height;

        double blockLengthRatio = (double) screenValue / (maxValue - minValue);

        int delta;

        if(axis == "x" || axis == "X") {
            delta = Math.toIntExact(Math.round(dragX / width * (maxValue - minValue) / blockLengthRatio));
        } else {
            delta = Math.toIntExact(Math.round(dragY / height * (maxValue - minValue) / blockLengthRatio));
        }

        value.set(Mth.clamp(value.get() + delta * (reverseDirection?-1:1), minValue, maxValue));

        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        isDragging = true;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        isDragging = false;
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = pos.toScreenX();
        int y = pos.toScreenY();

        this.width = size.toMenuX();
        this.height = size.toMenuY();

        // 根据占比动态填充矩形
        int screenValue;
        if(axis == "x" || axis == "X") screenValue = width; else screenValue = height;

        double blockLengthRatio = (double) screenValue / (maxValue - minValue);
        double blockStartRatio = (double) (value.get() - minValue) / (maxValue - minValue);
        if (reverseDirection) blockStartRatio = 1 - blockStartRatio;
        blockStartRatio = blockStartRatio * (1 - blockLengthRatio);

        if(axis == "x" || axis == "X") {
            graphics.fill(Math.toIntExact(Math.round(x + width * blockStartRatio)), y,
                    Math.toIntExact(Math.round(x + width * (blockStartRatio + blockLengthRatio))), y + height, color);
        } else {
            graphics.fill(x, Math.toIntExact(Math.round(y + height * blockStartRatio)), x + width,
                    Math.toIntExact(Math.round(y + height * (blockStartRatio + blockLengthRatio))), color);
        }
    }

    public static class DynamicScrollbar extends ScrollbarWidget {
        private final Coordinate region;

        public DynamicScrollbar(Coordinate pos, Coordinate size, Coordinate region, SyncedValue<Integer> value, int color, String axis, boolean reverseDirection) {
            super(pos, size, 0, 0, value, color, axis, reverseDirection);
            this.region = region;
        }

        private void syncRange() {
            this.minValue = region.toScreenX();
            this.maxValue = region.toScreenY();
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            syncRange();
            return super.mouseDragged(event, dragX, dragY);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            syncRange();
            super.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}
