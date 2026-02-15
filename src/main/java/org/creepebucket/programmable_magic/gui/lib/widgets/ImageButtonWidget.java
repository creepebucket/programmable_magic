package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

import java.util.function.Consumer;

/**
 * 图片按钮控件：显示普通/悬停两种状态的纹理，点击时触发回调。
 */
public class ImageButtonWidget extends Widget implements Renderable, Clickable {
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

    public ImageButtonWidget(Coordinate pos, Coordinate size, Identifier normal, Identifier hover, Runnable onPress) {
        this(pos, size, normal, hover, e -> onPress.run());
    }

    public ImageButtonWidget(Coordinate pos, Coordinate size, Identifier normal, Identifier hover, Consumer<MouseButtonEvent> onPress) {
        super(pos, size);
        this.normal = normal;
        this.hover = hover;
        this.onPress = onPress;
        renderInForeground = true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 判断鼠标是否悬停在按钮上，选择对应纹理
        boolean hovered = isInBounds(mouseX, mouseY);
        var tex = hovered ? this.hover : this.normal;

        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x(), y(), 0, 0, w(), h(), w(), h(), w(), h(), mainColor());
    }

    @Override
    public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
        // 检测点击是否在按钮范围内
        this.onPress.accept(event);
        return true;
    }
}
