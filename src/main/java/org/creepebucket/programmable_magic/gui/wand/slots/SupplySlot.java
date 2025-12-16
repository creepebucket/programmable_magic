package org.creepebucket.programmable_magic.gui.wand.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * 供应槽：不存储物品，展示并按需“复制”取出。
 */
public class SupplySlot extends Slot {
    private int supplyIndex;
    private boolean active = false;
    private final Supplier<List<ItemStack>> itemsSupplier;

    public SupplySlot(Container inv, int index, int x, int y, int supplyIndex, Supplier<List<ItemStack>> itemsSupplier) {
        super(inv, index, x, y);
        this.supplyIndex = supplyIndex;
        this.itemsSupplier = itemsSupplier;
    }

    public void setActive(boolean v) {
        this.active = v;
    }

    public void setSupplyIndex(int idx) {
        this.supplyIndex = idx;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private ItemStack supplyItem() {
        List<ItemStack> supplyItems = itemsSupplier.get();
        if (!active) return ItemStack.EMPTY;
        if (supplyIndex < 0 || supplyIndex >= supplyItems.size()) return ItemStack.EMPTY;
        ItemStack src = supplyItems.get(supplyIndex);
        if (src == null || src.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = src.copy();
        copy.setCount(1);
        return copy;
    }

    @Override
    public boolean hasItem() { return !supplyItem().isEmpty(); }

    @Override
    public ItemStack getItem() { return supplyItem(); }

    @Override
    public void set(ItemStack stack) { /* 供应槽不接收设置 */ }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) { return hasItem(); }

    @Override
    public ItemStack remove(int amount) {
        ItemStack it = supplyItem();
        if (it.isEmpty()) return ItemStack.EMPTY;
        int cnt = Math.max(1, Math.min(amount, it.getMaxStackSize()));
        ItemStack out = it.copy();
        out.setCount(cnt);
        return out;
    }

    @Override
    public void setChanged() { /* no-op */ }
}
