package org.creepebucket.programmable_magic.spells.old.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public abstract class MathOperationsSpell extends BaseComputeModLogic{
    public String registryName = "";
    public List<Component> tooltip;

    public MathOperationsSpell(String registryName, List<Component> tooltip) {
        this.registryName = registryName;
        this.tooltip = tooltip;
        this.RightParamsOffset = 1;
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public Component getSubCategory() {
        return Component.translatable("subcategory.programmable_magic.compute_number");
    }

    

    @Override
    public List<Component> getTooltip() {
        return tooltip;
    }


    public static class AdditionSpell extends MathOperationsSpell {
        public AdditionSpell() { super("compute_add", List.of(
                Component.translatable("tooltip.programmable_magic.spell.addition.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.addition.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 检查参数
            if (spellParams.get(0) instanceof Double) {
                double a = (Double) spellParams.get(0);
                double b = (Double) spellParams.get(1);
                return Map.of("successful", true, "type", NUMBER, "value", a + b);
            } else {
                return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(
                        ((Vec3) spellParams.get(0)).x + ((Vec3) spellParams.get(1)).x,
                        ((Vec3) spellParams.get(0)).y + ((Vec3) spellParams.get(1)).y,
                        ((Vec3) spellParams.get(0)).z + ((Vec3) spellParams.get(1)).z
                ));
            }
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(NUMBER, NUMBER),
                    List.of(VECTOR3, VECTOR3)
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(
                    List.of(NUMBER),
                    List.of(VECTOR3)
            );
        }
    }

    public static class SubtractionSpell extends MathOperationsSpell {
        public SubtractionSpell() { super("compute_sub", List.of(
                Component.translatable("tooltip.programmable_magic.spell.subtraction.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.subtraction.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 一元负号: (Number) -> (Number)
            if (spellParams.size() == 1 && spellParams.get(0) instanceof Double) {
                return Map.of("successful", true, "type", NUMBER, "value", -((Double) spellParams.get(0)));
            }
            // 二元减法
            if (spellParams.get(0) instanceof Double) {
                return Map.of("successful", true, "type", NUMBER, "value", (Double) spellParams.get(0) - (Double) spellParams.get(1));
            } else {
                return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(
                        ((Vec3) spellParams.get(0)).x - ((Vec3) spellParams.get(1)).x,
                        ((Vec3) spellParams.get(0)).y - ((Vec3) spellParams.get(1)).y,
                        ((Vec3) spellParams.get(0)).z - ((Vec3) spellParams.get(1)).z
                ));
            }
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(NUMBER, NUMBER),
                    List.of(VECTOR3, VECTOR3),
                    List.of(NUMBER)
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(
                    List.of(NUMBER),
                    List.of(VECTOR3),
                    List.of(NUMBER)
            );
        }
    }

    public static class MultiplicationSpell extends MathOperationsSpell {
        public MultiplicationSpell() { super("compute_mul", List.of(
                Component.translatable("tooltip.programmable_magic.spell.multiplication.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.multiplication.desc2"),
                Component.translatable("tooltip.programmable_magic.spell.multiplication.desc3")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 检查参数
            if (spellParams.get(0) instanceof Double && spellParams.get(1) instanceof Double) {
                return Map.of("successful", true, "type", NUMBER, "value", (Double) spellParams.get(0) * (Double) spellParams.get(1));
            } else if (spellParams.get(0) instanceof Vec3 && spellParams.get(1) instanceof Double) {
                return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(
                        ((Vec3) spellParams.get(0)).x * (Double) spellParams.get(1),
                        ((Vec3) spellParams.get(0)).y * (Double) spellParams.get(1),
                        ((Vec3) spellParams.get(0)).z * (Double) spellParams.get(1)
                ));
            } else {
                return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(
                        ((Vec3) spellParams.get(1)).x * (Double) spellParams.get(0),
                        ((Vec3) spellParams.get(1)).y * (Double) spellParams.get(0),
                        ((Vec3) spellParams.get(1)).z * (Double) spellParams.get(0)
                ));
            }
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(NUMBER, NUMBER),
                    List.of(VECTOR3, NUMBER),
                    List.of(NUMBER, VECTOR3)
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(
                    List.of(NUMBER),
                    List.of(VECTOR3),
                    List.of(VECTOR3)
            );
        }
    }

    public static class DivisionSpell extends MathOperationsSpell {
        public DivisionSpell() { super("compute_div", List.of(
                Component.translatable("tooltip.programmable_magic.spell.division.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.division.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 检查参数
            if (spellParams.get(0) instanceof Double && spellParams.get(1) instanceof Double) {
                return Map.of("successful", true, "type", NUMBER, "value", (Double) spellParams.get(0) / (Double) spellParams.get(1));
            } else if (spellParams.get(0) instanceof Vec3 && spellParams.get(1) instanceof Double) {
                return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(
                        ((Vec3) spellParams.get(0)).x / (Double) spellParams.get(1),
                        ((Vec3) spellParams.get(0)).y / (Double) spellParams.get(1),
                        ((Vec3) spellParams.get(0)).z / (Double) spellParams.get(1)
                ));
            } else {
                return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(
                        ((Vec3) spellParams.get(1)).x / (Double) spellParams.get(0),
                        ((Vec3) spellParams.get(1)).y / (Double) spellParams.get(0),
                        ((Vec3) spellParams.get(1)).z / (Double) spellParams.get(0)
                ));
            }
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(NUMBER, NUMBER),
                    List.of(VECTOR3, NUMBER),
                    List.of(NUMBER, VECTOR3)
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(
                    List.of(NUMBER),
                    List.of(VECTOR3),
                    List.of(VECTOR3)
            );
        }
    }

    public static class PowerSpell extends MathOperationsSpell {
        public PowerSpell() { super("compute_pow", List.of(
                Component.translatable("tooltip.programmable_magic.spell.power.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.power.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            return Map.of("successful", true, "type", NUMBER, "value", Math.pow((Double) spellParams.get(0), (Double) spellParams.get(1)));
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(
                    List.of(NUMBER, NUMBER)
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() {
            return List.of(
                    List.of(NUMBER)
            );
        }
    }

    public static class RemainderSpell extends MathOperationsSpell {
        public RemainderSpell() { super("compute_rem", List.of(
                Component.translatable("tooltip.programmable_magic.spell.remainder.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.remainder.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            return Map.of("successful", true, "type", NUMBER, "value", (Double) spellParams.get(0) % (Double) spellParams.get(1));
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(NUMBER, NUMBER)); }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
    }
}
