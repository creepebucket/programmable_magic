package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.world.inventory.Slot;
import org.creepebucket.programmable_magic.gui.lib.api.ClientSlotManager;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

public class SlotWidget extends Widget {

    public Slot slot;
    public Coordinate pos;

    public SlotWidget(Slot slot, Coordinate pos) {
        this.slot = slot;
        this.pos = pos;
    }

    @Override
    public void onTick() {
        ClientSlotManager.setClientPosition(this.slot, this.pos.toMenuX(), this.pos.toMenuY());
    }

    @Override
    public void onRemoved() {
        ClientSlotManager.removeClientPosition(this.slot);
    }
}

