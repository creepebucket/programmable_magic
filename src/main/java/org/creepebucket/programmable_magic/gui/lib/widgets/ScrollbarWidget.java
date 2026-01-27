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

    public int minValue, maxValue, color, bgColor, highLightColor;
    public SyncedValue<Integer> value;
    public boolean isDragging = false, reverseDirection;
    public String axis;

    public ScrollbarWidget(Coordinate pos, Coordinate size, int minValue, int maxValue, SyncedValue<Integer> value, int color, String axis, boolean reverseDirection) {
        super(pos, size);
        this.minValue = minValue;
        this.maxValue = maxValue;
        // 神秘
        this.color = (color & 16777215) | ((int)(((color >>> 24) * 0.6)) << 24);
        this.bgColor = (color & 16777215) | ((int)(((color >>> 24) * 0.2)) << 24);
        this.highLightColor = color;
        this.axis = axis;
        this.value = value;
        this.reverseDirection = reverseDirection;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!isDragging) return false;

        // 决定缩放因子和值修改
        // h/w -> max 映射

        int w = this.size.toMenuX();
        int h = this.size.toMenuY();

        int screenValue;
        if(axis == "x" || axis == "X") screenValue = w; else screenValue = h;

        double blockLengthRatio = (double) screenValue / (maxValue - minValue);

        int delta;

        if(axis == "x" || axis == "X") {
            delta = Math.toIntExact(Math.round(dragX / w * (maxValue - minValue) / (1 - blockLengthRatio)));
        } else {
            delta = Math.toIntExact(Math.round(dragY / h * (maxValue - minValue) / (1 - blockLengthRatio)));
        }

        value.set(Mth.clamp(value.get() + delta * (reverseDirection?-1:1), minValue, maxValue));

        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        if (!contains(event.x(), event.y())) return false;
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
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();

        int w = this.size.toScreenX();
        int h = this.size.toScreenY();

        // 根据占比动态填充矩形
        int screenValue;
        if(axis == "x" || axis == "X") screenValue = w; else screenValue = h;

        double blockLengthRatio = (double) screenValue / (maxValue - minValue);
        double blockStartRatio = (double) (value.get() - minValue) / (maxValue - minValue);
        if (reverseDirection) blockStartRatio = 1 - blockStartRatio;
        blockStartRatio = blockStartRatio * (1 - blockLengthRatio);

        int startX, startY, endX, endY;
        boolean horizontal = axis == "x" || axis == "X";

        if(horizontal) {
            startX = Math.toIntExact(Math.round(x + w * blockStartRatio));
            startY = y;
            endX = Math.toIntExact(Math.round(x + w * (blockStartRatio + blockLengthRatio)));
            endY = y + h;
        } else {
            startX = x;
            startY = Math.toIntExact(Math.round(y + h * blockStartRatio));
            endX = x + w;
            endY = Math.toIntExact(Math.round(y + h * (blockStartRatio + blockLengthRatio)));
        }

        // 背景
        graphics.fill(x, y, horizontal?startX:endX, horizontal?endY:startY, bgColor);
        graphics.fill(horizontal?endX:x, horizontal?y:endY, x + w, y + h, bgColor);

        // 主滚动条部分
        if(contains(mouseX, mouseY) || isDragging) {
            // 高亮
            graphics.fill(startX, startY, endX, endY, highLightColor);
        } else {
            graphics.fill(startX, startY, endX, endY, color);
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
