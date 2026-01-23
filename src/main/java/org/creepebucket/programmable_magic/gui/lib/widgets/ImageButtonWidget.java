package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.Consumer;

/**
 * 图片按钮控件：显示普通/悬停两种状态的纹理，点击时触发回调。
 */
public class ImageButtonWidget extends Widget implements Renderable, Clickable {
    /** 按钮左上角坐标 */
    private final Coordinate pos;
    /** 按钮宽度 */
    private final int width;
    /** 按钮高度 */
    private final int height;
    /** 普通状态纹理 */
    private final Identifier normal;
    /** 悬停状态纹理 */
    private final Identifier hover;
    /** 点击回调 */
    private final Consumer<MouseButtonEvent> onPress;

    public ImageButtonWidget(Coordinate pos, int width, int height, Identifier normal, Identifier hover, Runnable onPress) {
        this(pos, width, height, normal, hover, e -> onPress.run());
    }

    public ImageButtonWidget(Coordinate pos, int width, int height, Identifier normal, Identifier hover, Consumer<MouseButtonEvent> onPress) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.normal = normal;
        this.hover = hover;
        this.onPress = onPress;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 判断鼠标是否悬停在按钮上，选择对应纹理
        int sx = this.pos.toScreenX();
        int sy = this.pos.toScreenY();
        boolean hovered = isInBounds(mouseX, mouseY, sx, sy, this.width, this.height);
        var tex = hovered ? this.hover : this.normal;
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.pos.toMenuX(), this.pos.toMenuY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        // 检测点击是否在按钮范围内
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        if (!isInBounds(event.x(), event.y(), x, y, this.width, this.height)) return false;
        this.onPress.accept(event);
        return true;
    }
}
