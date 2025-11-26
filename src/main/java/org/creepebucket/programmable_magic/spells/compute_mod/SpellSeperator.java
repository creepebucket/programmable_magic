package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;

import java.util.List;
import java.util.Map;

public class SpellSeperator extends BaseComputeModLogic{
    @Override
    public String getRegistryName() {
        return "compute_mod";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful", true);
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {
        return List.of();
    }

    @Override
    public List<Object> getNeededParamsType() {
        return List.of();
    }
}
