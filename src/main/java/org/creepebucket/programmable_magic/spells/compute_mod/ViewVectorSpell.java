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

import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public class ViewVectorSpell extends BaseComputeModLogic {
    @Override
    public String getRegistryName() {
        return "compute_view_vec";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        Vec3 viewDirection = player.getLookAngle().normalize();
        return Map.of("successful", true, "type", VECTOR3, "value", viewDirection);
    }

    @Override
    public void calculateBaseMana(SpellData data) {
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(Component.translatable("item.programmable_magic.spell_display_compute_view_vec"));
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.<SpellValueType>of());
    }
}
