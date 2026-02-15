package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils.WandValues;

public class SpellSupplyLogicPlugin extends WandPluginLogic {
    public final int tier;

    public SpellSupplyLogicPlugin(int tier) {
        this.tier = tier;
    }

    @Override
    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
        values.manaMult = Math.pow(0.95, tier - 1);
    }
}
