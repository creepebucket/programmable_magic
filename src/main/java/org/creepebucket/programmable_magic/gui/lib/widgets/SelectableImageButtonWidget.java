package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.BooleanSupplier;

public class SelectableImageButtonWidget extends Widget {
    private final Coordinate pos;
    private final int width;
    private final int height;
    private final Identifier normal;
    private final Identifier selected;
    private final BooleanSupplier isSelected;
    private final Runnable onPress;

    public SelectableImageButtonWidget(Coordinate pos, int width, int height, Identifier normal, Identifier selected, BooleanSupplier isSelected, Runnable onPress) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.normal = normal;
        this.selected = selected;
        this.isSelected = isSelected;
        this.onPress = onPress;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var tex = this.isSelected.getAsBoolean() ? this.selected : this.normal;
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.pos.toMenuX(), this.pos.toMenuY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        double mx = event.x();
        double my = event.y();
        if (mx < x || mx >= x + this.width || my < y || my >= y + this.height) return false;
        this.onPress.run();
        return true;
    }
}
