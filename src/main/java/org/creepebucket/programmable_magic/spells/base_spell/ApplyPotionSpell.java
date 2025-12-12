package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.creepebucket.programmable_magic.spells.*;

import java.util.List;
import java.util.Map;

public class ApplyPotionSpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "apply_potion"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.status"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        LivingEntity target = (LivingEntity) spellParams.get(0);
        ItemStack stack = (ItemStack) spellParams.get(1);
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return Map.of("successful", true);

        contents.potion().ifPresent(holder -> {
            var potion = holder.value();
            for (MobEffectInstance effect : potion.getEffects()) {
                target.addEffect(new MobEffectInstance(effect));
            }
        });

        for (MobEffectInstance effect : contents.customEffects()) {
            target.addEffect(new MobEffectInstance(effect));
        }

        return Map.of("successful", true);
    }

    @Override
    public Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new Mana(0.0, 0.0, 0.3, 0.3);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.apply_potion.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.apply_potion.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.ENTITY, SpellValueType.ITEM));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.SPELL));
    }
}
