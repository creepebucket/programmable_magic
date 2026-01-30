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
import java.util.function.Consumer;

/**
 * 图片按钮控件：显示普通/悬停两种状态的纹理，点击时触发回调。
 */
public class ImageButtonWidget extends Widget implements Renderable, Clickable, Tooltipable {
    /**
     * 普通状态纹理
     */
    private final Identifier normal;
    /**
     * 悬停状态纹理
     */
    private final Identifier hover;
    /**
     * 点击回调
     */
    private final Consumer<MouseButtonEvent> onPress;
    /**
     * tooltip文本
     */
    private final Component tooltip;

    public ImageButtonWidget(Coordinate pos, Coordinate size, Identifier normal, Identifier hover, Runnable onPress, Component tooltip) {
        this(pos, size, normal, hover, e -> onPress.run(), tooltip);
    }

    public ImageButtonWidget(Coordinate pos, Coordinate size, Identifier normal, Identifier hover, Consumer<MouseButtonEvent> onPress, Component tooltip) {
        super(pos, size);
        this.normal = normal;
        this.hover = hover;
        this.onPress = onPress;
        this.tooltip = tooltip;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 判断鼠标是否悬停在按钮上，选择对应纹理
        boolean hovered = contains(mouseX, mouseY);
        var tex = hovered ? this.hover : this.normal;
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.pos.toMenuX(), this.pos.toMenuY(), 0, 0, w, h, w, h);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        // 检测点击是否在按钮范围内
        if (!contains(event.x(), event.y())) return false;
        this.onPress.accept(event);
        return true;
    }

    @Override
    public boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.renderTooltip(
                ClientUiContext.getFont(),
                List.of(ClientTooltipComponent.create(this.tooltip.getVisualOrderText())),
                mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null
        );
        return true;
    }
}
