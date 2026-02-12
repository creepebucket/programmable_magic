package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.slots.OneItemOnlySlot;

public class WandSlots {
    public static class CustomSupplySlot extends OneItemOnlySlot {
        public SyncedValue<Boolean> supplyMode; // true = 供应 false = 设置

        public CustomSupplySlot(Container container, int slot, int x, int y, SyncedValue<Boolean> supplyMode) {
            super(container, slot, x, y);
            this.supplyMode = supplyMode;
        }

        @Override
        public ItemStack remove(int amount) {
            if (supplyMode.get()) return this.getItem().copy();
            else return super.remove(amount);
        }

        @Override
        public void set(ItemStack stack) {
            if (!supplyMode.get()) super.set(stack);
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            if (!supplyMode.get()) super.setByPlayer(stack);
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack oldStack) {
            if (!supplyMode.get()) super.setByPlayer(stack, oldStack);
        }
    }
}
