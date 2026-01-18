package org.creepebucket.programmable_magic.spells.old.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.*;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;

import java.util.List;
import java.util.Map;

public class PlaceBlockSpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "place_block"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.world_interaction"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        ItemStack stack = (ItemStack) spellParams.get(0);
        if (!(stack.getItem() instanceof BlockItem bi)) return Map.of("successful", true);
        Block block = bi.getBlock();
        BlockPos pos = BlockPos.containing(data.getPosition());
        player.level().setBlockAndUpdate(pos, block.defaultBlockState());
        return Map.of("successful", true);
    }

    @Override
    public ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new ModUtils.Mana(0.0, 0.0, 0.3, 0.0);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.place_block.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.place_block.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.ITEM)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.SPELL)); }
}
