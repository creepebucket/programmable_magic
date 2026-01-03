package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.ENTITY;
import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public abstract class EntityQuerySpell extends BaseComputeModLogic {
    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.entity_query"); }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(ENTITY)); }

    public static class EntityPosSpell extends EntityQuerySpell {
        @Override
        public String getRegistryName() { return "compute_entity_pos"; }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Entity e = (Entity) spellParams.get(0);
            Vec3 pos = e.position();
            return Map.of("successful", true, "type", VECTOR3, "value", pos);
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.entity_pos.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.entity_pos.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(VECTOR3)); }
    }

    public static class EntityHealthSpell extends EntityQuerySpell {
        @Override
        public String getRegistryName() { return "compute_entity_health"; }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            LivingEntity e = (LivingEntity) spellParams.get(0);
            return Map.of("successful", true, "type", NUMBER, "value", (double) e.getHealth());
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.entity_health.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.entity_health.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
    }

    public static class EntityMaxHealthSpell extends EntityQuerySpell {
        @Override
        public String getRegistryName() { return "compute_entity_max_health"; }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            LivingEntity e = (LivingEntity) spellParams.get(0);
            return Map.of("successful", true, "type", NUMBER, "value", (double) e.getMaxHealth());
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.entity_max_health.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.entity_max_health.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
    }

    public static class EntityArmorSpell extends EntityQuerySpell {
        @Override
        public String getRegistryName() { return "compute_entity_armor"; }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            LivingEntity e = (LivingEntity) spellParams.get(0);
            return Map.of("successful", true, "type", NUMBER, "value", (double) e.getArmorValue());
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.entity_armor.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.entity_armor.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
    }
}

