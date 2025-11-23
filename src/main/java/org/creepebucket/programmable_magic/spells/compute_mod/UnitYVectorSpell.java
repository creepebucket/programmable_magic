package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;

public class UnitYVectorSpell extends BaseVectorOutputSpell {
    private static final Vec3 VALUE = new Vec3(0, 1, 0);

    @Override
    public String getRegistryName() { return "compute_unit_y"; }

    @Override
    protected Vec3 resolveVector(Player player, SpellData data) { return VALUE; }

    @Override
    protected Component describe() {
        return Component.literal("Y 轴单位向量");
    }
}
