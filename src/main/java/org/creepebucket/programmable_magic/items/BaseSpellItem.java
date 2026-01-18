package org.creepebucket.programmable_magic.items;

import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;

public class BaseSpellItem extends Item {
    private final SpellItemLogic logic;

    public BaseSpellItem(Properties properties, SpellItemLogic logic) {
        super(properties);
        this.logic = logic;
    }

    public SpellItemLogic getLogic() {
        return logic;
    }
} 