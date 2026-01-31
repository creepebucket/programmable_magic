package org.creepebucket.programmable_magic.gui.lib.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OneItemOnlySlot extends Slot {
    public OneItemOnlySlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }
}
