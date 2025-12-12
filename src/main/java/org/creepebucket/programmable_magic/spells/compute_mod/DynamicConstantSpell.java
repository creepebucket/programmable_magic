package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.EMPTY;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;
import static org.creepebucket.programmable_magic.spells.SpellValueType.ENTITY;

public abstract class DynamicConstantSpell extends BaseComputeModLogic {
    /*
     * 动态常量
     * 虽然不是常量, 但是这里的法术均为 () -> (Any), 将其视为常量
     */

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(EMPTY));
    }

    

    // 通用提示：动态常量
    protected static List<Component> baseDynamicTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.dynamic_constant.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.dynamic_constant.desc2")
        );
    }

    // 子类专属说明，默认空
    protected List<Component> getSpecificTooltip() { return List.of(); }

    @Override
    public List<Component> getTooltip() {
        var tip = new java.util.ArrayList<Component>(baseDynamicTooltip());
        tip.addAll(getSpecificTooltip());
        return tip;
    }

    @Override
    public Component getSubCategory() {
        return Component.translatable("subcategory.programmable_magic.dynamic_constant");
    }

    public static class ViewVectorSpell extends DynamicConstantSpell {
        public String getRegistryName() {
            return "compute_view_vec";
        }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 viewDirection = player.getLookAngle().normalize();
            return Map.of("successful", true, "type", VECTOR3, "value", viewDirection);
        }

        protected List<Component> getSpecificTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.view_vector.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.view_vector.desc2")
            );
        }

        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(List.of(VECTOR3));
        }
    }

    public static class CasterPosSpell extends DynamicConstantSpell {
        public String getRegistryName() {
            return "compute_caster_pos";
        }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 pos = player.position();
            return Map.of("successful", true, "type", VECTOR3, "value", pos);
        }

        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(List.of(VECTOR3));
        }

        protected List<Component> getSpecificTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.caster_pos.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.caster_pos.desc2")
            );
        }
    }

    public static class CasterEntitySpell extends DynamicConstantSpell {
        public String getRegistryName() {
            return "compute_caster";
        }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            return Map.of("successful", true, "type", ENTITY, "value", player);
        }

        protected List<Component> getSpecificTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.caster_entity.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.caster_entity.desc2")
            );
        }

        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(List.of(ENTITY));
        }
    }

    public static class SpellPosSpell extends DynamicConstantSpell {
        public String getRegistryName() {
            return "compute_spell_pos";
        }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 pos = data.getPosition();
            return Map.of("successful", true, "type", VECTOR3, "value", pos);
        }

        protected List<Component> getSpecificTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.spell_pos.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.spell_pos.desc2")
            );
        }

        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(List.of(VECTOR3));
        }
    }

    public static class SpellEntitySpell extends DynamicConstantSpell {
        public String getRegistryName() {
            return "compute_spell_entity";
        }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Entity self = data.getCustomData("spell_entity", Entity.class);
            return Map.of("successful", true, "type", ENTITY, "value", self);
        }

        protected List<Component> getSpecificTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.spell_entity.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.spell_entity.desc2")
            );
        }

        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(List.of(ENTITY));
        }
    }

    public static class NearestEntitySpell extends DynamicConstantSpell {
        public String getRegistryName() { return "compute_nearest_entity"; }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            var pos = data.getPosition();
            var self = data.getCustomData("spell_entity", Entity.class);
            double r = 16.0; // 简单范围
            AABB box = new net.minecraft.world.phys.AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);
            List<Entity> list = player.level().getEntities((Entity) null, box, e -> e != null && e != self && e != player);

            Entity nearest = null;
            double best = Double.POSITIVE_INFINITY;
            for (Entity e : list) {
                double d = e.distanceToSqr(pos.x, pos.y, pos.z);
                if (d < best) { best = d; nearest = e; }
            }
            if (nearest == null) nearest = self;
            return Map.of("successful", true, "type", ENTITY, "value", nearest);
        }

        @Override
        protected List<Component> getSpecificTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.nearest_entity.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.nearest_entity.desc2")
            );
        }

        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(ENTITY)); }
    }
}
