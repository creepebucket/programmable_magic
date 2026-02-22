package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.slots.OneItemOnlySlot;
import org.creepebucket.programmable_magic.registries.ModItems;

import java.util.HashMap;
import java.util.Map;

import static org.creepebucket.programmable_magic.registries.WandPluginRegistry.isPlugin;

public class WandSlots {
    public static class CustomSupplySlot extends OneItemOnlySlot {
        public SyncedValue<Boolean> supplyMode; // true = 供应 false = 设置

        public CustomSupplySlot(Container container, int slot, int x, int y, SyncedValue<Boolean> supplyMode) {
            super(container, slot, x, y);
            this.supplyMode = supplyMode;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !supplyMode.get() && stack.is(ModItems.PACKED_SPELL);
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

    public static class PluginSlot extends OneItemOnlySlot {

        public PluginSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!isPlugin(stack.getItem())) return false;

            Map<Object, Boolean> map = new HashMap<>();
            for (ItemStack plugin : container) {
                if (plugin.isEmpty()) continue;
                var s = BuiltInRegistries.ITEM.getKey(plugin.getItem()).toString();
                map.put(s.substring(0, s.length() - 4), true);
            }

            var s = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return stack.isEmpty() || !map.getOrDefault(s.substring(0, s.length() - 4), false);
        }
    }

    public static class InventorySlot extends Slot {

        public InventorySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public void set(ItemStack stack) {
            if (mayPlace(stack)) super.set(stack);
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            if (mayPlace(stack)) super.setByPlayer(stack);
        }

        @Override
        public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
            if (mayPlace(newStack)) super.setByPlayer(newStack, oldStack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !getItem().is(ModItems.WAND);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !getItem().is(ModItems.WAND);
        }
    }
}
