package org.creepebucket.programmable_magic.gui.wand.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;

/**
 * 卷轴制作右槽：只读输出。
 */
public class ScrollOutputSlot extends Slot {
    private final WandMenu wandMenu;

    public ScrollOutputSlot(WandMenu wandMenu, Container inv, int index, int x, int y) {
        super(inv, index, x, y);
        this.wandMenu = wandMenu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasItem() {
        return !this.container.getItem(this.getSlotIndex()).isEmpty();
    }

    @Override
    public ItemStack getItem() {
        return this.container.getItem(this.getSlotIndex());
    }

    @Override
    public void set(ItemStack stack) { /* 禁止外部写入 */ }

    @Override
    public ItemStack remove(int amount) {
        return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        // 消耗左侧一张纸
        ItemStack in = this.container.getItem(0);
        if (!in.isEmpty()) {
            in.shrink(1);
            if (in.isEmpty()) this.container.setItem(0, ItemStack.EMPTY);
        }
        this.container.setChanged();
    }
}
