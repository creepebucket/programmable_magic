package org.creepebucket.programmable_magic.gui.wand.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 卷轴制作左槽：仅允许纸张。
 */
public class ScrollInputSlot extends Slot {
    public ScrollInputSlot(Container inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Items.PAPER);
    }

    @Override
    public void setChanged() {
        this.container.setChanged();
    }
}
