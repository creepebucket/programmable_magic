package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class BreakBlockSpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "break_block"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.world_interaction"); }

    @Override
    public boolean isExecutable() { return false; }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        BlockPos pos = BlockPos.containing(data.getPosition());
        BlockState state = player.level().getBlockState(pos);
        player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        return Map.of("successful", true, "type", SpellValueType.ITEM, "value", new ItemStack(state.getBlock()));
    }

    @Override
    public ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new ModUtils.Mana();
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.break_block.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.break_block.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(SpellValueType.EMPTY)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.ITEM)); }
}

