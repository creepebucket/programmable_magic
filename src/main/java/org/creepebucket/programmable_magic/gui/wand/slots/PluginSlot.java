package org.creepebucket.programmable_magic.gui.wand.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;

/**
 * 插件槽：仅允许放入已注册的魔杖插件物品。
 */
public class PluginSlot extends Slot {

    public PluginSlot(Container inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack != null && WandPluginRegistry.isPlugin(stack.getItem()) && this.container.canPlaceItem(this.getSlotIndex(), stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return super.mayPickup(player);
    }
}

