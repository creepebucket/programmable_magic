package org.creepebucket.programmable_magic.spells;

import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

public class PackedSpell extends Item {
    public PackedSpell(Properties properties) {
        super(properties.component(ModDataComponents.RESOURCE_LOCATION, "item/packed_spell_default.png"));
    }
}
