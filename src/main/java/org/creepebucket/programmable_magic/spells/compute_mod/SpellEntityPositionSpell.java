package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;

public class SpellEntityPositionSpell extends BaseVectorOutputSpell {
    @Override
    public String getRegistryName() { return "compute_spell_pos"; }

    @Override
    protected Vec3 resolveVector(Player player, SpellData data) {
        if (data != null && data.getPosition() != null) {
            return data.getPosition();
        }
        return player != null ? new Vec3(player.getX(), player.getEyeY(), player.getZ()) : null;
    }

    @Override
    protected Component describe() {
        return Component.literal("法术载体位置");
    }
}
