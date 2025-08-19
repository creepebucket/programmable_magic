package org.creepebucket.programmable_magic.items.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

import java.util.List;

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