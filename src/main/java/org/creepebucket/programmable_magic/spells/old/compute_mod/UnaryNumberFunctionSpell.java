package org.creepebucket.programmable_magic.spells.old.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public abstract class UnaryNumberFunctionSpell extends MathOperationsSpell {
    public UnaryNumberFunctionSpell(String registryName, List<Component> tooltip) { super(registryName, tooltip); }

    protected abstract double apply(double v);

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        double v = (Double) spellParams.get(0);
        return Map.of("successful", true, "type", NUMBER, "value", apply(v));
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(NUMBER)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }

    public static class SinSpell extends UnaryNumberFunctionSpell {
        public SinSpell() { super("compute_sin", List.of(
                Component.translatable("tooltip.programmable_magic.spell.sin.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.sin.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.sin(v); }
    }

    public static class CosSpell extends UnaryNumberFunctionSpell {
        public CosSpell() { super("compute_cos", List.of(
                Component.translatable("tooltip.programmable_magic.spell.cos.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.cos.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.cos(v); }
    }

    public static class TanSpell extends UnaryNumberFunctionSpell {
        public TanSpell() { super("compute_tan", List.of(
                Component.translatable("tooltip.programmable_magic.spell.tan.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.tan.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.tan(v); }
    }

    public static class AsinSpell extends UnaryNumberFunctionSpell {
        public AsinSpell() { super("compute_asin", List.of(
                Component.translatable("tooltip.programmable_magic.spell.asin.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.asin.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.asin(v); }
    }

    public static class AcosSpell extends UnaryNumberFunctionSpell {
        public AcosSpell() { super("compute_acos", List.of(
                Component.translatable("tooltip.programmable_magic.spell.acos.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.acos.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.acos(v); }
    }

    public static class AtanSpell extends UnaryNumberFunctionSpell {
        public AtanSpell() { super("compute_atan", List.of(
                Component.translatable("tooltip.programmable_magic.spell.atan.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.atan.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.atan(v); }
    }

    public static class CeilSpell extends UnaryNumberFunctionSpell {
        public CeilSpell() { super("compute_ceil", List.of(
                Component.translatable("tooltip.programmable_magic.spell.ceil.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.ceil.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.ceil(v); }
    }

    public static class FloorSpell extends UnaryNumberFunctionSpell {
        public FloorSpell() { super("compute_floor", List.of(
                Component.translatable("tooltip.programmable_magic.spell.floor.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.floor.desc2")
        )); }

        @Override
        protected double apply(double v) { return Math.floor(v); }
    }
}
