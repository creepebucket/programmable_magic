package org.creepebucket.programmable_magic.spells.adjust_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据前面表达式运算结果，提升后续 BASE 法术的威力与耗魔为对应倍数。
 * - 表达式结果记为 f；有效值：f >= 0，否则按 0 处理。
 * - 将 data.power *= f
 * - 将四系耗魔 *= f
 */
public class PowerBoostSpell extends BaseAdjustModLogic {

    @Override
    public String getRegistryName() {
        return "power_boost"; // 注意下划线
    }

    @Override
    public boolean run(Player player, SpellData data) {
        // 在运行期也立即应用威力与耗魔倍数，确保后续基础法术能读到提升后的 power
        applyManaModification(data);
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        // 调整类自身不产生基础耗魔
    }

    @Override
    public void applyManaModification(SpellData data) {
        if (data == null) return;
        Double result = data.getCustomData("compute_result", Double.class);
        double f = result != null ? result : 0.0;
        if (Double.isNaN(f) || Double.isInfinite(f)) f = 0.0;
        if (f < 0) f = 0.0;

        // 威力倍乘
        data.setPower(data.getPower() * f);
        // 耗魔倍乘
        for (String manaType : new String[]{"radiation", "temperature", "momentum", "pressure"}) {
            data.multiplyManaCost(manaType, f);
        }
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.spell_modifier"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.power_boost_by_expression"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost_multiplier_by_expression"));
        return tooltip;
    }
}
