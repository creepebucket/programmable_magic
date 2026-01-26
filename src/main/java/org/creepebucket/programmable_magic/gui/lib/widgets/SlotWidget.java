package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.world.inventory.Slot;
import org.creepebucket.programmable_magic.gui.lib.api.ClientSlotManager;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

/**
 * 槽位控件：每 tick 将 {@link Slot} 的客户端坐标同步到 {@link ClientSlotManager}。
 */
public class SlotWidget extends Widget implements Tickable, Lifecycle {
    /** 关联的槽位实例 */
    public Slot slot;

    /**
     * 创建一个槽位控件。
     */
    public SlotWidget(Slot slot, Coordinate pos) {
        super(pos);
        this.slot = slot;
    }

    /**
     * 持续更新槽位的客户端坐标。
     */
    @Override
    public void onInitialize() {
        tick();
    }

    @Override
    public void tick() {
        ClientSlotManager.setClientPosition(this.slot, this.pos.toMenuX(), this.pos.toMenuY());
    }

    /**
     * 控件移除时清理槽位坐标映射。
     */
    @Override
    public void onRemoved() {
        ClientSlotManager.removeClientPosition(this.slot);
    }
}
