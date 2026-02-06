package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tooltipable;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * 可选中图片按钮控件：根据选中状态显示不同纹理，点击时触发回调。
 */
public class SelectableImageButtonWidget extends Widget implements Renderable, Clickable, Tooltipable {
    private final Identifier normal;
    private final Identifier selected;
    private final Component tooltip;
    public boolean isSelected = false;

    public SelectableImageButtonWidget(Coordinate pos, Coordinate size, Identifier normal, Identifier selected, Component tooltip) {
        super(pos, size);
        this.normal = normal;
        this.selected = selected;
        this.tooltip = tooltip;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 根据选中状态选择纹理并渲染
        var tex = isSelected ? this.selected : this.normal;
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.pos.toMenuX(), this.pos.toMenuY(), 0, 0, w, h, w, h);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        // 检测点击是否在按钮范围内
        if (!contains(event.x(), event.y())) return false;
        isSelected = !isSelected;
        return true;
    }

    @Override
    public boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!contains(mouseX, mouseY)) return false;

        graphics.renderTooltip(
                ClientUiContext.getFont(),
                List.of(ClientTooltipComponent.create(this.tooltip.getVisualOrderText())),
                mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null
        );
        return true;
    }
}
