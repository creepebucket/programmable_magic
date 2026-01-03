package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.*;

import java.util.List;
import java.util.Map;

public class PaintDataSpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "paint_data"; }

    @Override
    public net.minecraft.network.chat.Component getSubCategory() { return net.minecraft.network.chat.Component.translatable("subcategory.programmable_magic.debug"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        Object v = spellParams.get(0);
        SpellValueType t = inferType(v);
        String valueStr = stringifyValue(v);

        ((ServerPlayer) player).sendSystemMessage(Component.literal("[paint] ").append(t.typed()).append(": ").append(Component.literal(valueStr).withStyle(t.color())));
        return Map.of("successful", true);
    }

    @Override
    public ModUtils.Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new ModUtils.Mana(0.05, 0.05, 0.05, 0.05);
    }

    @Override
    public List<Component> getTooltip() { return List.of(); }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.ANY));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.SPELL)); }

    private static SpellValueType inferType(Object v) {
        if (v instanceof Boolean) return SpellValueType.BOOLEAN;
        if (v instanceof Number) return SpellValueType.NUMBER;
        if (v instanceof Vec3) return SpellValueType.VECTOR3;
        if (v instanceof Entity) return SpellValueType.ENTITY;
        if (v instanceof net.minecraft.world.item.ItemStack) return SpellValueType.ITEM;
        if (v instanceof BlockState) return SpellValueType.BLOCK;
        return SpellValueType.ANY;
    }

    private static String stringifyValue(Object v) {
        if (v instanceof Vec3 vec) {
            return String.format(java.util.Locale.ROOT, "(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z);
        }
        return String.valueOf(v);
    }
}
