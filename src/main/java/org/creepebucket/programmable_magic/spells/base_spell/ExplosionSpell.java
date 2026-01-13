package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.*;

import java.util.List;
import java.util.Map;

public class ExplosionSpell extends BaseBaseSpellLogic{
    @Override
    public String getRegistryName() {
        return "explosion";
    }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.world_interaction"); }

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
    public ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new ModUtils.Mana(0.0, (Double) spellParams.get(0) / 2, 0.0, (Double) spellParams.get(0) / 2);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.explosion.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.explosion.desc2"),
                Component.translatable("tooltip.programmable_magic.spell.explosion.desc3")
        );
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
