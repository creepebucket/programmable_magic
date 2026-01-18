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

public class BuildVectorSpell extends BaseComputeModLogic {
    public BuildVectorSpell() { this.RightParamsOffset = 0; }

    @Override
    public String getRegistryName() { return "compute_vec_xyz"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.compute_vector"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        double x = (Double) spellParams.get(0);
        double y = (Double) spellParams.get(1);
        double z = (Double) spellParams.get(2);
        return Map.of("successful", true, "type", VECTOR3, "value", new Vec3(x, y, z));
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.build_vector.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.build_vector.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(NUMBER, NUMBER, NUMBER)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(VECTOR3)); }
}

