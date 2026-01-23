package org.creepebucket.programmable_magic.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.items.BaseSpellItem;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class SpellTooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BaseSpellItem spellItem)) {
            return;
        }

        SpellItemLogic logic = spellItem.getLogic();
        List<Component> tooltip = event.getToolTip();

        // 在这里添加法术的tooltip
        MutableComponent tmp; // 临时变量

        // 1. 基本信息
        // TODO: 本地化

        // 法术类型
        if (logic instanceof SpellItemLogic.BaseSpell) tmp = Component.literal("基础法术");
        else if (logic instanceof SpellItemLogic.ComputeMod) tmp = Component.literal("计算修饰");
        else if (logic instanceof SpellItemLogic.ControlMod) tmp = Component.literal("控制修饰");
        else if (logic instanceof SpellItemLogic.AdjustMod) tmp = Component.literal("调整修饰");
        else tmp = Component.literal("未知法术");

        // 参数偏移
        tmp.append(Component.literal(" / 右置" + logic.rightParamOffset + "参"));

        // 优先级
        tmp.append(Component.literal(" / 优先级" + logic.precedence));

        // 结合性
        if (logic.rightConnectivity) tmp.append(Component.literal(" / 右结合性"));
                else tmp.append(Component.literal(" / 左结合性"));

        tooltip.add(tmp.withStyle(ChatFormatting.GRAY));

        // 重载

        tooltip.add(Component.literal("重载:").withStyle(ChatFormatting.AQUA));

        for (int i = 0; i < logic.inputTypes.size(); i++) { // 应该不会NPE
            tmp = Component.literal("    (").withStyle(ChatFormatting.GRAY);

            // 输入
            var types = logic.inputTypes.get(i);
            var tmp2 = Component.empty();
            for (int j = 0; j < types.size(); j++) {
                if (j != 0) tmp2.append(Component.literal(", ").withStyle(ChatFormatting.GRAY)); // 傻逼特判
                tmp2.append(types.get(j).typed());
            }
            tmp.append(tmp2);

            tmp.append(Component.literal(") -> (").withStyle(ChatFormatting.GRAY));

            // 输出
            types = logic.outputTypes.get(i);
            tmp2 = Component.empty();
            for (int j = 0; j < types.size(); j++) {
                if (j != 0) tmp2.append(Component.literal(", ").withStyle(ChatFormatting.GRAY)); // 依旧傻逼特判
                tmp2.append(types.get(j).typed());
            }
            tmp.append(tmp2);

            tooltip.add(tmp.append(Component.literal(")").withStyle(ChatFormatting.GRAY)));
        }

        // 描述

        tooltip.add(Component.literal("描述:").withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(Component.translatable("spell." + MODID + "." + logic.name + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
