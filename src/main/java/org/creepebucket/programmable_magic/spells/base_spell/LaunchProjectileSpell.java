package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;
import org.creepebucket.programmable_magic.util.WeightUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 发射法术左侧提供的弹射物：当前实现仅按类型为法术实体增加质量，
 * 以便后续动量/传送类法术使用真实质量计算耗能。
 * 参数支持：Item / ItemStack / 物品ID字符串（minecraft:snowball 等）。
 */
public class LaunchProjectileSpell extends BaseSpellEffectLogic {
    @Override
    public String getRegistryName() { return "launch_projectile"; }

    @Override
    public boolean run(Player player, SpellData data) {
        if (data == null) return true;
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) return true;

        ComputeValue any = ComputeRuntime.findLeftValue(player, data, idx, null);
        if (!(data.getTarget() instanceof SpellEntity se)) return true;
        if (any == null) return true;

        Item item = WeightUtil.tryParseItem(any.value());
        if (item == null) {
            ComputeRuntime.sendError(player, "发射物参数无效: 需要物品/ID");
            return true;
        }
        double add = WeightUtil.massForProjectileItem(item);
        se.setWeightKg(Math.max(0.0, se.getWeightKg() + add));
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        // 仅设置质量，不额外耗魔
        data.setManaCost("momentum", 0.0);
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        in.add(SpellValueType.MODIFIER);
        in.add(SpellValueType.ANY);
        Component desc = Component.literal("发射弹射物: 左侧提供物品；按类型增加法术实体质量");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.SPELL, desc, this);
    }
}

