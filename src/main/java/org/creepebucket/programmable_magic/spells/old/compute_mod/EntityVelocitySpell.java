package org.creepebucket.programmable_magic.spells.old.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.ENTITY;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public class EntityVelocitySpell extends BaseComputeModLogic {

    @Override
    public String getRegistryName() { return "compute_entity_velocity"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.compute_vector"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        Entity e = (Entity) spellParams.get(0);
        Vec3 dir = e.getDeltaMovement();
        return Map.of("successful", true, "type", VECTOR3, "value", dir);
    }

    

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.entity_velocity.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.entity_velocity.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(ENTITY));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(VECTOR3));
    }
}
