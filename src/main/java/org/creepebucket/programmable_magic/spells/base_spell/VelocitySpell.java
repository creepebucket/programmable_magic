package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.*;

import java.util.List;
import java.util.Map;

public class VelocitySpell extends BaseBaseSpellLogic{
    @Override
    public String getRegistryName() {
        return "gain_velocity";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 给予实体速度
        Entity target = (Entity) spellParams.get(0);
        target.push((Vec3) spellParams.get(1));
        target.hurtMarked = true;

        return Map.of("successful", true);
    }

    @Override
    public Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new Mana(0.0, 0.0, ((Vec3) spellParams.get(1)).length(), 0.0);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.velocity.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.velocity.desc2"),
                Component.translatable("tooltip.programmable_magic.spell.velocity.desc3")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.ENTITY, SpellValueType.VECTOR3));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.SPELL));
    }
}
