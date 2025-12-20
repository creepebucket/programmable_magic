package org.creepebucket.programmable_magic.gui.wand.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;

/**
 * 插件槽：仅允许放入已注册的魔杖插件物品。
 */
public class PluginSlot extends Slot {

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
