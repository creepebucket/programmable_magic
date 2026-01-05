package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;

import java.util.List;
import java.util.function.Supplier;

public class WandSlots {
    /**
     * 插件槽：仅允许放入已注册的魔杖插件物品。
     */
    public static final class PluginSlot extends Slot {

        public PluginSlot(Container inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        /**
         * 仅允许注册过的插件物品放入。
         */
        public boolean mayPlace(ItemStack stack) {
            BasePlugin incoming = WandPluginRegistry.createPlugin(stack.getItem());
            if (incoming == null) return false;
            Class<?> inClazz = incoming.getClass();

            int size = this.container.getContainerSize();
            for (int i = 0; i < size; i++) {
                if (i == this.getSlotIndex()) continue;
                ItemStack cur = this.container.getItem(i);
                if (cur == null || cur.isEmpty()) continue;
                BasePlugin exist = WandPluginRegistry.createPlugin(cur.getItem());
                if (exist == null) continue;
                Class<?> exClazz = exist.getClass();
                if (exClazz.isAssignableFrom(inClazz) || inClazz.isAssignableFrom(exClazz)) return false;
            }
            return this.container.canPlaceItem(this.getSlotIndex(), stack);
        }

        @Override
        /**
         * 默认允许拾取。
         */
        public boolean mayPickup(Player player) {
            return super.mayPickup(player);
        }
    }

    /**
     * 法术栏槽位：通过全局偏移映射至 wandInv 的实际索引。
     */
    public static final class OffsetSlot extends Slot {
        private final WandMenu wandMenu;
        private final Container inv;
        private final int baseIndex;

        public OffsetSlot(WandMenu wandMenu, Container inv, int baseIndex, int x, int y) {
            super(inv, 0, x, y);
            this.wandMenu = wandMenu;
            this.inv = inv;
            this.baseIndex = baseIndex;
        }

        /**
         * 目标索引 = 视窗基准 + 全局偏移
         */
        private int targetIndex() {
            return baseIndex + wandMenu.spellIndexOffset;
        }

        /**
         * 检查索引是否在容器有效范围内
         */
        private boolean inRange(int idx) {
            return idx >= 0 && idx < inv.getContainerSize();
        }

        @Override
        public boolean isActive() {
            return inRange(targetIndex());
        }

        @Override
        public boolean hasItem() {
            int idx = targetIndex();
            return inRange(idx) && !inv.getItem(idx).isEmpty();
        }

        @Override
        public ItemStack getItem() {
            int idx = targetIndex();
            return inRange(idx) ? inv.getItem(idx) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            int idx = targetIndex();
            if (inRange(idx)) inv.setItem(idx, stack);
            setChanged();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            int idx = targetIndex();
            if (!inRange(idx)) return false;
            var item = stack.getItem();
            boolean isSpell = SpellRegistry.isSpell(item);
            boolean isPlaceholder = item instanceof WandItemPlaceholder;
            return (isSpell || isPlaceholder) && this.container.canPlaceItem(idx, stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            int idx = targetIndex();
            return inRange(idx) && super.mayPickup(player);
        }

        @Override
        public ItemStack remove(int amount) {
            int idx = targetIndex();
            return inRange(idx) ? inv.removeItem(idx, amount) : ItemStack.EMPTY;
        }

        @Override
        public void setChanged() {
            this.inv.setChanged();
        }
    }

    /**
     * 供应槽：不存储物品，展示并按需“复制”取出。
     */
    public static final class SupplySlot extends Slot {
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
}

