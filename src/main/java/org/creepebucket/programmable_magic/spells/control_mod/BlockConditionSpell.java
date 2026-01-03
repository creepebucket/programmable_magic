package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.BLOCK;
import static org.creepebucket.programmable_magic.spells.SpellValueType.BOOLEAN;

public abstract class BlockConditionSpell extends BaseControlModLogic {
    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.block_condition"); }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(BLOCK)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(BOOLEAN)); }

    public static class IsAirSpell extends BlockConditionSpell {
        @Override
        public String getRegistryName() { return "is_air"; }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            BlockState state = (BlockState) spellParams.get(0);
            return Map.of("successful", true, "type", BOOLEAN, "value", state.isAir());
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.is_air.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.is_air.desc2")
            );
        }
    }

    public static class IsGravityBlockSpell extends BlockConditionSpell {
        @Override
        public String getRegistryName() { return "is_gravity_block"; }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            BlockState state = (BlockState) spellParams.get(0);
            return Map.of("successful", true, "type", BOOLEAN, "value", state.getBlock() instanceof FallingBlock);
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.is_gravity_block.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.is_gravity_block.desc2")
            );
        }
    }
}

