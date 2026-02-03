package org.creepebucket.programmable_magic.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
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
        String typeKey;
        if (logic instanceof SpellItemLogic.BaseSpell) typeKey = "tooltip." + MODID + ".spell_tooltip.type.base_spell";
        else if (logic instanceof SpellItemLogic.ComputeMod) typeKey = "tooltip." + MODID + ".spell_tooltip.type.compute_mod";
        else if (logic instanceof SpellItemLogic.ControlMod) typeKey = "tooltip." + MODID + ".spell_tooltip.type.control_mod";
        else if (logic instanceof SpellItemLogic.AdjustMod) typeKey = "tooltip." + MODID + ".spell_tooltip.type.adjust_mod";
        else typeKey = "tooltip." + MODID + ".spell_tooltip.type.unknown";

        String assocKey = logic.rightConnectivity
                ? "tooltip." + MODID + ".spell_tooltip.assoc.right"
                : "tooltip." + MODID + ".spell_tooltip.assoc.left";

        tooltip.add(Component.translatable(
                "tooltip." + MODID + ".spell_tooltip.summary",
                Component.translatable(typeKey),
                logic.precedence,
                Component.translatable(assocKey)
        ).withStyle(ChatFormatting.GRAY));

        // 重载

        tooltip.add(Component.translatable("tooltip." + MODID + ".spell_tooltip.overloads").withStyle(ChatFormatting.AQUA));

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

        tooltip.add(Component.translatable("tooltip." + MODID + ".spell_tooltip.description").withStyle(ChatFormatting.LIGHT_PURPLE));

        String descBaseKey = "tooltip." + MODID + ".spell." + logic.name + ".";
        tooltip.add(Component.translatable(descBaseKey + "desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(descBaseKey + "desc2").withStyle(ChatFormatting.GRAY));
        String desc3Key = descBaseKey + "desc3";
        if (I18n.exists(desc3Key)) tooltip.add(Component.translatable(desc3Key).withStyle(ChatFormatting.GRAY));
    }
}
