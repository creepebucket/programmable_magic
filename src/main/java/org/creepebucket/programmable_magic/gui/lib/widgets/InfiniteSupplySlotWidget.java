package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.slots.InfiniteSupplySlot;

/**
 * 无限供给槽位：槽位内物品永远保持为指定物品 * 1。
 * <p>
 * 行为效果（依赖原版 {@link net.minecraft.world.inventory.AbstractContainerMenu} 点击逻辑）：
 * - 鼠标游标为空时点击：拿到该物品（数量 1），槽位不变
 * - 鼠标游标有任意物品时点击：游标物品被替换为该物品（原游标物品被丢弃），槽位不变
 */
public class InfiniteSupplySlotWidget extends SlotWidget {
    public InfiniteSupplySlotWidget(ItemStack supplyStack, Coordinate pos) {
        super(new InfiniteSupplySlot(supplyStack), pos);
    }
}
