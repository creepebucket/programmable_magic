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

import static org.creepebucket.programmable_magic.spells.SpellValueType.EMPTY;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public abstract class DynamicConstantSpell extends BaseComputeModLogic {
    /*
     * 动态常量
     * 虽然不是常量, 但是这里的法术均为 () -> (Any), 将其视为常量
     */

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(EMPTY));
    }

    @Override
    public void calculateBaseMana(SpellData data) {
    }

    public static class ViewVectorSpell extends DynamicConstantSpell {
        public String getRegistryName() {
            return "compute_view_vec";
        }

        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 viewDirection = player.getLookAngle().normalize();
            return Map.of("successful", true, "type", VECTOR3, "value", viewDirection);
        }

        public List<Component> getTooltip() {
            return List.of(Component.translatable("item.programmable_magic.spell_display_compute_view_vec"));
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

        public List<Component> getTooltip() {
            return List.of(Component.translatable("item.programmable_magic.spell_display_compute_caster_pos"));
        }
    }
}
