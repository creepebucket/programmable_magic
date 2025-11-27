package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class ExplosionSpell extends BaseBaseSpellLogic{
    @Override
    public String getRegistryName() {
        return "explosion";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        float strength = (float) Math.cbrt(Math.max(0.0D, ((Number) spellParams.get(0)).doubleValue()));
        player.level().explode(
                (Entity) player,
                data.getPosition().x,
                data.getPosition().y,
                data.getPosition().z,
                strength,
                true,
                Level.ExplosionInteraction.TNT
        );

        return Map.of("successful",  true);
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {
        return List.of();
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.NUMBER));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.SPELL));
    }
}
