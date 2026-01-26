package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ValueLiteralSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public Object value;
    public SpellValueType type;

    public ValueLiteralSpell(SpellValueType type, Object value, String name) {
        this.type = type;
        this.value = value;
        this.name = name;
    }

    public ValueLiteralSpell(SpellValueType type, Object value) {
        this(type, value, "value_literal");
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return ExecutionResult.SUCCESS(this);
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }

    // 一些常量

    public static class PiSpell extends ValueLiteralSpell {
        public PiSpell() {
            super(SpellValueType.NUMBER, Math.PI, "pi");
            subCategory = "spell." + MODID + ".subcategory.constants.number";
        }
    }

    public static class XUnitVectorSpell extends ValueLiteralSpell {
        public XUnitVectorSpell() {
            super(SpellValueType.VECTOR3, new Vec3(1, 0, 0), "x_unit_vector");
            subCategory = "spell." + MODID + ".subcategory.constants.vector";
        }
    }

    public static class YUnitVectorSpell extends ValueLiteralSpell {
        public YUnitVectorSpell() {
            super(SpellValueType.VECTOR3, new Vec3(0, 1, 0), "y_unit_vector");
            subCategory = "spell." + MODID + ".subcategory.constants.vector";
        }
    }

    public static class ZUnitVectorSpell extends ValueLiteralSpell {
        public ZUnitVectorSpell() {
            super(SpellValueType.VECTOR3, new Vec3(0, 0, 1), "z_unit_vector");
            subCategory = "spell." + MODID + ".subcategory.constants.vector";
        }
    }

    public static class TrueSpell extends ValueLiteralSpell {
        public TrueSpell() {
            super(SpellValueType.BOOLEAN, true, "true");
            subCategory = "spell." + MODID + ".subcategory.constants.boolean";
        }
    }

    public static class FalseSpell extends ValueLiteralSpell {
        public FalseSpell() {
            super(SpellValueType.BOOLEAN, false, "false");
            subCategory = "spell." + MODID + ".subcategory.constants.boolean";
        }
    }
}
