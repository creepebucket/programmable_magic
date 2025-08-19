package org.creepebucket.programmable_magic.spells.target_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

import java.util.ArrayList;
import java.util.List;

public class ProjectileSpell extends BaseTargetModLogic {
    
    public ProjectileSpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "projectile";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        // 投射物载体不执行特殊逻辑，只是标记法术为投射物形式
        data.setCustomData("isProjectile", true);
        data.setRange(500.0); // 增加射程
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) {
        // 载体法术本身不消耗魔力
    }
    
    @Override
    public void applyManaModification(SpellData data) {
        // 投射物载体稍微增加动量系魔力消耗
        data.addManaCost("momentum", 1.0);
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.spell_carrier"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost"));
        tooltip.add(Component.literal("  Momentum: +1.0"));
        return tooltip;
    }
} 