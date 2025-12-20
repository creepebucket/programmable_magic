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

    /**
     * 设置该槽是否参与渲染与交互。
     */
    public void setActive(boolean v) {
        this.active = v;
    }

    /**
     * 指定该槽映射到供应列表中的索引。
     */
    public void setSupplyIndex(int idx) {
        this.supplyIndex = idx;
    }

    @Override
    /**
     * 是否激活（用于隐藏/占位）。
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 返回当前映射的供应物品（一份只读拷贝）。
     */
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
    /**
     * 仅在存在映射物品时显示。
     */
    public boolean hasItem() { return !supplyItem().isEmpty(); }

    @Override
    /**
     * 提供一个只读展示用的物品拷贝。
     */
    public ItemStack getItem() { return supplyItem(); }

    @Override
    /**
     * 禁止设置。
     */
    public void set(ItemStack stack) { /* 供应槽不接收设置 */ }

    @Override
    /**
     * 禁止外部放入。
     */
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    /**
     * 允许从中“取出”复制品。
     */
    public boolean mayPickup(Player player) { return hasItem(); }

    @Override
    /**
     * 取出时返回限定数量的复制品，不影响来源列表。
     */
    public ItemStack remove(int amount) {
        ItemStack it = supplyItem();
        if (it.isEmpty()) return ItemStack.EMPTY;
        int cnt = Math.max(1, Math.min(amount, it.getMaxStackSize()));
        ItemStack out = it.copy();
        out.setCount(cnt);
        return out;
    }

    @Override
    /**
     * 供应槽无内部状态，不触发变更。
     */
    public void setChanged() { /* no-op */ }
}
