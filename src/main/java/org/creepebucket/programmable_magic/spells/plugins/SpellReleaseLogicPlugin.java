package org.creepebucket.programmable_magic.spells.plugins;

import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils.WandValues;

public class SpellReleaseLogicPlugin extends WandPluginLogic {
    public final int tier;

    public SpellReleaseLogicPlugin(int tier) {
        this.tier = tier;
    }

    @Override
    public void adjustWandValues(WandValues values, ItemStack pluginStack) {
        values.chargeRateW += Math.pow(8, tier - 1) * 1024;
    }
}
