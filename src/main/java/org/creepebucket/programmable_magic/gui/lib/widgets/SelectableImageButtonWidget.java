package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.BooleanSupplier;

/**
 * 可选中图片按钮控件：根据选中状态显示不同纹理，点击时触发回调。
 */
public class SelectableImageButtonWidget extends Widget implements Renderable, Clickable {
    /** 按钮左上角坐标 */
    private final Coordinate pos;
    /** 按钮宽度 */
    private final int width;
    /** 按钮高度 */
    private final int height;
    /** 未选中状态纹理 */
    private final Identifier normal;
    /** 选中状态纹理 */
    private final Identifier selected;
    /** 是否选中的状态提供器 */
    private final BooleanSupplier isSelected;
    /** 点击回调 */
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 根据选中状态选择纹理并渲染
        var tex = this.isSelected.getAsBoolean() ? this.selected : this.normal;
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.pos.toMenuX(), this.pos.toMenuY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        // 检测点击是否在按钮范围内
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        if (!isInBounds(event.x(), event.y(), x, y, this.width, this.height)) return false;
        this.onPress.run();
        return true;
    }
}
