package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaintDataSpell extends BaseSpellEffectLogic {

    @Override
    public String getRegistryName() {
        return "paint_data";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        if (player == null || data == null) return true;

        // 基础信息
        String pos = String.format("(%.2f, %.2f, %.2f)", data.getPosition().x, data.getPosition().y, data.getPosition().z);
        String dir = String.format("(%.2f, %.2f, %.2f)", data.getDirection().x, data.getDirection().y, data.getDirection().z);
        Entity target = data.getTarget();
        String targetInfo = target == null ? "null" : target.getClass().getSimpleName() + "#" + target.getId();

        // 逐行输出到聊天栏，保持简洁
        player.displayClientMessage(Component.literal("SpellData:"), false);
        player.displayClientMessage(Component.literal("  power=" + data.getPower() + ", range=" + data.getRange() + ", delay=" + data.getDelay()), false);
        player.displayClientMessage(Component.literal("  active=" + data.isActive() + ", pos=" + pos + ", dir=" + dir), false);
        player.displayClientMessage(Component.literal("  target=" + targetInfo), false);

        // 魔力消耗
        Map<String, Double> mana = data.getAllManaCosts();
        player.displayClientMessage(Component.literal("  mana:"), false);
        player.displayClientMessage(Component.literal("    radiation: " + ModUtils.FormattedManaString(mana.getOrDefault("radiation", 0.0))), false);
        player.displayClientMessage(Component.literal("    temperature: " + ModUtils.FormattedManaString(mana.getOrDefault("temperature", 0.0))), false);
        player.displayClientMessage(Component.literal("    momentum: " + ModUtils.FormattedManaString(mana.getOrDefault("momentum", 0.0))), false);
        player.displayClientMessage(Component.literal("    pressure: " + ModUtils.FormattedManaString(mana.getOrDefault("pressure", 0.0))), false);

        // 自定义数据（打印所有键与简单值）
        Map<String, Object> custom = data.getAllCustomData();
        if (custom.isEmpty()) {
            player.displayClientMessage(Component.literal("  custom: <empty>"), false);
        } else {
            player.displayClientMessage(Component.literal("  custom:"), false);
            int shown = 0;
            for (Map.Entry<String, Object> e : custom.entrySet()) {
                // 仅打印简单toString，避免过长
                if (shown >= 6) { // 避免刷屏
                    player.displayClientMessage(Component.literal("    ..."), false);
                    break;
                }
                player.displayClientMessage(Component.literal("    " + e.getKey() + ": " + String.valueOf(e.getValue())), false);
                shown++;
            }
        }

        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        // 四系各 0.01
        data.setManaCost("radiation", 0.01);
        data.setManaCost("temperature", 0.01);
        data.setManaCost("momentum", 0.01);
        data.setManaCost("pressure", 0.01);
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost"));
        tooltip.add(Component.literal("  Radiation: " + ModUtils.FormattedManaString(0.01)));
        tooltip.add(Component.literal("  Temperature: " + ModUtils.FormattedManaString(0.01)));
        tooltip.add(Component.literal("  Momentum: " + ModUtils.FormattedManaString(0.01)));
        tooltip.add(Component.literal("  Pressure: " + ModUtils.FormattedManaString(0.01)));
        return tooltip;
    }
}
