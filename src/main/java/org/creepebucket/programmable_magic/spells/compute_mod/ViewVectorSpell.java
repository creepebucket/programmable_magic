package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;

public class ViewVectorSpell extends BaseVectorOutputSpell {
    @Override
    public String getRegistryName() { return "compute_view_vec"; }

    @Override
    protected Vec3 resolveVector(Player player, SpellData data) {
        if (data != null && data.getDirection() != null) {
            return data.getDirection();
        }
        return player != null ? player.getLookAngle() : null;
    }

    @Override
    protected Component describe() {
        return Component.literal("视角方向单位向量");
    }
}
