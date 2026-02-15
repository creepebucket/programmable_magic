package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import org.creepebucket.programmable_magic.gui.lib.api.ClientSlotManager;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

/**
 * 槽位控件：每 tick 将 {@link Slot} 的客户端坐标同步到 {@link ClientSlotManager}。
 */
public class SlotWidget extends Widget implements Lifecycle, Renderable {
    /**
     * 关联的槽位实例
     */
    public Slot slot;

    /**
     * 创建一个槽位控件。
     */
    public SlotWidget(Slot slot, Coordinate pos) {
        super(pos, Coordinate.ZERO);
        this.slot = slot;
    }

    /**
     * 持续更新槽位的客户端坐标。
     */
    @Override
    public void onInitialize() {
        ClientSlotManager.setClientPosition(this.slot, menuX(), menuY());
    }

    /**
     * 控件移除时清理槽位坐标映射。
     */
    @Override
    public void onDestroy() {
        ClientSlotManager.removeClientPosition(this.slot);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientSlotManager.setClientPosition(this.slot, menuX(), menuY());
    }
}
