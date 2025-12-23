package org.creepebucket.programmable_magic.gui.wand.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

/**
 * 法术栏槽位：通过全局偏移映射至 wandInv 的实际索引。
 */
public class OffsetSlot extends Slot {
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
