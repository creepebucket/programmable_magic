package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SpellStorageSlot extends SlotItemHandler {
    public final WandMenu menu;

    public SpellStorageSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, WandMenu menu) {
        super(itemHandler, index, xPosition, yPosition);
        this.menu = menu;
    }

    public void handleClick() {
        if (!this.getItem().isEmpty()) {
            this.getItemHandler().extractItem(this.getSlotIndex(), 64, false);
        }
    }

    public boolean tryInsertItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemStack remaining = this.getItemHandler().insertItem(this.getSlotIndex(), stack, false);
            return remaining.getCount() < stack.getCount();
        }
        return false;
    }
}
