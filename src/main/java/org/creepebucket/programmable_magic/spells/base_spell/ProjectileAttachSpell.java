package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import org.creepebucket.programmable_magic.spells.*;

import java.util.List;
import java.util.Map;

public class ProjectileAttachSpell extends BaseBaseSpellLogic {
    @Override
    public String getRegistryName() { return "projectile_attach"; }

    @Override
    public net.minecraft.network.chat.Component getSubCategory() { return net.minecraft.network.chat.Component.translatable("subcategory.programmable_magic.projectile"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 参数：一个金属粒（使用 NeoForge 通用粒标签校验）
        if (spellParams == null || spellParams.isEmpty()) return Map.of("successful", true);
        ItemStack pellet = (ItemStack) spellParams.get(0);
        if (pellet == null || pellet.isEmpty()) return Map.of("successful", true);

        // 仅接受通用 NUGGETS 标签的物品（覆盖原版金粒/铁粒，也兼容其他模组）
        if (!pellet.is(Tags.Items.NUGGETS)) {
            return Map.of("successful", true); // 不生效，但不中断序列
        }

        // 标记 SpellEntity：由实体在 tick 中严格检测碰撞并结算伤害
        data.setCustomData("projectile_attach_active", Boolean.TRUE);
        data.setCustomData("projectile_attach_item", pellet.copy());

        return Map.of("successful", true);
    }

    @Override
    public Mana calculateBaseMana(SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return new Mana(0.1, 0.1, 0.1, 0.1);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.projectile_attach.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.projectile_attach.desc2"),
                Component.translatable("tooltip.programmable_magic.spell.projectile_attach.desc3")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.ITEM));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.SPELL));
    }
}
