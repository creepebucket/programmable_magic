package org.creepebucket.programmable_magic.gui.lib.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfiniteSupplySlot extends Slot {
    private final ItemStack supplyStack;

    public InfiniteSupplySlot(ItemStack supplyStack) {
        super(new SimpleContainer(1), 0, 0, 0);
        this.supplyStack = supplyStack.copyWithCount(1);
    }

    @Override
    public ItemStack getItem() {
        return this.supplyStack.copy();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.supplyStack.copy();
    }

    @Override
    public void set(ItemStack stack) {
    }

    @Override
    public void setByPlayer(ItemStack stack) {
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack oldStack) {
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (ItemStack.isSameItemSameComponents(stack, this.supplyStack)) return 1;
        return stack.getMaxStackSize();
    }
}
