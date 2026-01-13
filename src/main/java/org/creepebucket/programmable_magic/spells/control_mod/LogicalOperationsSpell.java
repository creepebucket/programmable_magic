package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public abstract class LogicalOperationsSpell extends BaseControlModLogic {
    public LogicalOperationsSpell() { this.RightParamsOffset = 1; }

    @Override
    public boolean isExecutable() { return false; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.compute_boolean"); }

    

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.BOOLEAN));
    }

    public static class EqualSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() {
            return "equal";
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", spellParams.get(0).equals(spellParams.get(1)));
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(List.of(SpellValueType.ANY, SpellValueType.ANY));
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.equal.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.equal.desc2")
        ); }
    }

    public static class NotEqualSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() {
            return "not_equal";
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", !spellParams.get(0).equals(spellParams.get(1)));
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(List.of(SpellValueType.ANY, SpellValueType.ANY));
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.not_equal.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.not_equal.desc2")
        ); }
    }

    public static class GreaterSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() {
            return "greater";
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),
                    List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3)
            );
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            if (spellParams.get(0) instanceof Double) {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", (Double) spellParams.get(0) > (Double) spellParams.get(1));
            } else { // 若为向量则比较模长
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", ((Vec3) spellParams.get(0)).length() > ((Vec3) spellParams.get(1)).length());
            }
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.greater.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.greater.desc2")
        ); }
    }

    public static class GreaterEqualSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() {
            return "greater_equal";
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),
                    List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3)
            );
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            if (spellParams.get(0) instanceof Double) {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", (Double) spellParams.get(0) >= (Double) spellParams.get(1));
            } else {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", ((Vec3) spellParams.get(0)).length() >= ((Vec3) spellParams.get(1)).length());
            }
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.greater_equal.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.greater_equal.desc2")
        ); }
    }

    public static class LessSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() { return "less"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),
                    List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3)
            );
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            if (spellParams.get(0) instanceof Double) {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", (Double) spellParams.get(0) < (Double) spellParams.get(1));
            } else {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", ((Vec3) spellParams.get(0)).length() < ((Vec3) spellParams.get(1)).length());
            }
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.less.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.less.desc2")
        ); }
    }

    public static class LessEqualSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() { return "less_equal"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),
                    List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3)
            );
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            if (spellParams.get(0) instanceof Double) {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", (Double) spellParams.get(0) <= (Double) spellParams.get(1));
            } else {
                return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", ((Vec3) spellParams.get(0)).length() <= ((Vec3) spellParams.get(1)).length());
            }
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.less_equal.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.less_equal.desc2")
        ); }
    }

    public static class AndSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() { return "and"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.BOOLEAN, SpellValueType.BOOLEAN)); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            boolean a = Boolean.TRUE.equals(spellParams.get(0));
            boolean b = Boolean.TRUE.equals(spellParams.get(1));
            return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", a && b);
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.and.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.and.desc2")
        ); }
    }

    public static class OrSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() { return "or"; }
        
        @Override
        public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.BOOLEAN, SpellValueType.BOOLEAN)); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            boolean a = Boolean.TRUE.equals(spellParams.get(0));
            boolean b = Boolean.TRUE.equals(spellParams.get(1));
            return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", a || b);
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.or.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.or.desc2")
        ); }
    }

    public static class NotSpell extends LogicalOperationsSpell {
        @Override
        public String getRegistryName() { return "not"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.BOOLEAN)); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            boolean a = Boolean.TRUE.equals(spellParams.get(0));
            return Map.of("successful", true, "type", SpellValueType.BOOLEAN, "value", !a);
        }

        @Override
        public List<Component> getTooltip() { return List.of(
                Component.translatable("tooltip.programmable_magic.spell.not.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.not.desc2")
        ); }
}
}
