package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public abstract class MathOpreationsSpell extends BaseComputeModLogic{
    public String registryName = "";
    public List<Component> tooltip;

    public MathOpreationsSpell(String registryName, List<Component> tooltip) {
        this.registryName = registryName;
        this.tooltip = tooltip;
        this.RightParamsOffset = 1;
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {
        return tooltip;
    }


    public static class AdditionSpell extends MathOpreationsSpell {
        public AdditionSpell() { super("compute_add", List.of(
                Component.translatable("tooltip.programmable_magic.spell.addition.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.addition.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 检查参数
            if (spellParams.get(0) instanceof Number) {
                return Map.of("successful", true, "type", NUMBER, "value", (Double) spellParams.get(0) + (Double) spellParams.get(1));
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

    public static class SubtractionSpell extends MathOpreationsSpell {
        public SubtractionSpell() { super("compute_sub", List.of(
                Component.translatable("tooltip.programmable_magic.spell.subtraction.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.subtraction.desc2")
        )); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 检查参数
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

    public static class MultiplicationSpell extends MathOpreationsSpell {
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

    public static class DivisionSpell extends MathOpreationsSpell {
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

    public static class PowerSpell extends MathOpreationsSpell {
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
}
