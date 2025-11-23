package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;

public class CasterPositionSpell extends BaseVectorOutputSpell {
    @Override
    public String getRegistryName() { return "compute_caster_pos"; }

    @Override
    protected Vec3 resolveVector(Player player, SpellData data) {
        if (data == null) return player != null ? player.position() : null;
        Player caster = data.getCaster() != null ? data.getCaster() : player;
        if (caster == null) return null;
        return new Vec3(caster.getX(), caster.getEyeY(), caster.getZ());
    }

    @Override
    protected Component describe() {
        return Component.literal("施法者位置 (Vector3)");
    }
}
