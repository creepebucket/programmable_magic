package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SmoothedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseDraggable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class ScrollbarWidget extends Widget implements MouseDraggable, Clickable, Renderable {

    public SmoothedValue value;
    public boolean isDragging = false, reverseDirection;
    public String axis;
    public Coordinate region;

    public ScrollbarWidget(Coordinate pos, Coordinate size, Coordinate region, SmoothedValue value, String axis) {
        super(pos, size);
        this.region = region;
        this.axis = axis;
        this.value = value;
    }

    public ScrollbarWidget reverseDirection() {
        reverseDirection = !reverseDirection;
        return this;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!isDragging) return false;

        // 决定缩放因子和值修改
        // h/w -> max 映射

        var maxValue = region.toScreenY();
        var minValue = region.toScreenX();

        int screenValue;
        if (axis == "x" || axis == "X") screenValue = w();
        else screenValue = h();

        double blockLengthRatio = (double) screenValue / (maxValue - minValue);

        int delta;

        if (axis == "x" || axis == "X") {
            delta = Math.toIntExact(Math.round(dragX / w() * (maxValue - minValue) / (1 - blockLengthRatio)));
        } else {
            delta = Math.toIntExact(Math.round(dragY / h() * (maxValue - minValue) / (1 - blockLengthRatio)));
        }

        value.set(Mth.clamp(value.target + delta * (reverseDirection ? -1 : 1), minValue, maxValue));

        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        if (!isInBounds(event.x(), event.y())) return false;
        isDragging = true;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!isDragging) return false;
        isDragging = false;
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 根据占比动态填充矩形

        var maxValue = region.toScreenY();
        var minValue = region.toScreenX();

        int screenValue;
        if (axis == "x" || axis == "X") screenValue = w();
        else screenValue = h();

        double blockLengthRatio = (double) screenValue / (maxValue - minValue);
        double blockStartRatio = (value.get() - minValue) / (maxValue - minValue);
        if (reverseDirection) blockStartRatio = 1 - blockStartRatio;
        blockStartRatio = blockStartRatio * (1 - blockLengthRatio);

        int startX, startY, endX, endY;
        boolean horizontal = axis == "x" || axis == "X";

        if (horizontal) {
            startX = Math.toIntExact(Math.round(x() + w() * blockStartRatio));
            startY = y();
            endX = Math.toIntExact(Math.round(x() + w() * (blockStartRatio + blockLengthRatio)));
            endY = y() + h();
        } else {
            startX = x();
            startY = Math.toIntExact(Math.round(y() + h() * blockStartRatio));
            endX = x() + w();
            endY = Math.toIntExact(Math.round(y() + h() * (blockStartRatio + blockLengthRatio)));
        }

        // 背景
        graphics.fill(x(), y(), horizontal ? startX : endX, horizontal ? endY : startY, bgColor());
        graphics.fill(horizontal ? endX : x(), horizontal ? y() : endY, x() + w(), y() + h(), bgColor());

        // 主滚动条部分
        if (isInBounds(mouseX, mouseY) || isDragging) {
            // 高亮
            graphics.fill(startX, startY, endX, endY, mainColor());
        } else {
            graphics.fill(startX, startY, endX, endY, new Color(mainColor()).toArgbWithAlphaMult(0.6));
        }
    }
}
