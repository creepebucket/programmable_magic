package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tooltipable;

import java.util.List;

/**
 * 可选中图片按钮控件：根据选中状态显示不同纹理，点击时触发回调。
 */
public class SelectableImageButtonWidget extends Widget implements Renderable, Clickable, Tooltipable {
    private final Identifier normal;
    public boolean isSelected = false;
    private Identifier selected;

    public SelectableImageButtonWidget(Coordinate pos, Coordinate size, Identifier texture) {
        super(pos, size);
        this.normal = texture;
        this.selected = texture;
    }

    public SelectableImageButtonWidget selectedTexture(Identifier texture) {
        selected = texture;
        return this;
    }

    public SelectableImageButtonWidget defaultSelected() {
        this.isSelected = true;
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 根据选中状态选择纹理并渲染
        var tex = isSelected ? this.selected : this.normal;
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x(), y(), 0, 0, w(), h(), w(), h(), w(), h(), mainColor());
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        isSelected = !isSelected;
        return true;
    }

    @Override
    public boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isInBounds(mouseX, mouseY)) return false;

        graphics.renderTooltip(
                ClientUiContext.getFont(),
                List.of(ClientTooltipComponent.create(this.tooltip.getVisualOrderText())),
                mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null
        );
        return true;
    }
}
