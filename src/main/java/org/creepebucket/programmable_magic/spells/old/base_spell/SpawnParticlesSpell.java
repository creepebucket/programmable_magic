package org.creepebucket.programmable_magic.spells.old.base_spell;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class SpawnParticlesSpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "spawn_particles"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.show_effect"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        Vec3 pos = (Vec3) spellParams.get(0);
        Vec3 rgb = (Vec3) spellParams.get(1);
        ServerLevel serverLevel = (ServerLevel) player.level();
        var opt = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) rgb.x, (float) rgb.y, (float) rgb.z);
        serverLevel.sendParticles(opt, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        return Map.of("successful", true);
    }

    @Override
    public ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new ModUtils.Mana(0.0, 0.0, 0.05, 0.0);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.spawn_particles.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.spawn_particles.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.SPELL)); }
}
