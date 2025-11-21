package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleComputeSpell extends BaseComputeModLogic {

    @Override
    public boolean run(Player player, SpellData data) {
        // 逻辑暂时不写，占位
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        // 四系各 0.005
        data.setManaCost("radiation", 0.005);
        data.setManaCost("temperature", 0.005);
        data.setManaCost("momentum", 0.005);
        data.setManaCost("pressure", 0.005);
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost"));
        tooltip.add(Component.literal("  Radiation: " + ModUtils.FormattedManaString(0.005)));
        tooltip.add(Component.literal("  Temperature: " + ModUtils.FormattedManaString(0.005)));
        tooltip.add(Component.literal("  Momentum: " + ModUtils.FormattedManaString(0.005)));
        tooltip.add(Component.literal("  Pressure: " + ModUtils.FormattedManaString(0.005)));
        return tooltip;
    }
}

