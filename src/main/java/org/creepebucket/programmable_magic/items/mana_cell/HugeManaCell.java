package org.creepebucket.programmable_magic.items.mana_cell;

import net.minecraft.world.item.Item;

public class HugeManaCell extends BaseManaCell {
    public HugeManaCell(Item.Properties properties) {
        super(properties, 2.048 * 8 * 8 * 8, 1);
    }
}
