package org.creepebucket.programmable_magic.items;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.api.ModItemExtensions;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class BaseSpellItem extends Item implements ModItemExtensions {
    private final SpellItemLogic logic;

    public BaseSpellItem(Properties properties, SpellItemLogic logic) {
        super(properties);
        this.logic = logic;
    }

    public SpellItemLogic getLogic() {
        return logic;
    }

    @Override
    public void append_tooltip(ItemStack stack, List<Component> tooltip, boolean ctrl, boolean shift, boolean alt) {
        SpellItemLogic logic = getLogic();

        MutableComponent tmp;

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

        tooltip.add(Component.translatable("tooltip." + MODID + ".spell_tooltip.overloads").withStyle(ChatFormatting.AQUA));

        for (int i = 0; i < logic.inputTypes.size(); i++) {
            tmp = Component.literal("    (").withStyle(ChatFormatting.GRAY);

            var types = logic.inputTypes.get(i);
            var tmp2 = Component.empty();
            if (!types.isEmpty()) {
                tmp2.append(types.get(0).typed());
                for (int j = 1; j < types.size(); j++) {
                    tmp2.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    tmp2.append(types.get(j).typed());
                }
            }
            tmp.append(tmp2);

            tmp.append(Component.literal(") -> (").withStyle(ChatFormatting.GRAY));

            types = logic.outputTypes.get(i);
            tmp2 = Component.empty();
            if (!types.isEmpty()) {
                tmp2.append(types.get(0).typed());
                for (int j = 1; j < types.size(); j++) {
                    tmp2.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    tmp2.append(types.get(j).typed());
                }
            }
            tmp.append(tmp2);

            tooltip.add(tmp.append(Component.literal(")").withStyle(ChatFormatting.GRAY)));
        }

        tooltip.add(Component.translatable("tooltip." + MODID + ".spell_tooltip.description").withStyle(ChatFormatting.LIGHT_PURPLE));

        String descBaseKey = "tooltip." + MODID + ".spell." + logic.name + ".";
        tooltip.add(Component.translatable(descBaseKey + "desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(descBaseKey + "desc2").withStyle(ChatFormatting.GRAY));
        String desc3Key = descBaseKey + "desc3";
        if (Language.getInstance().has(desc3Key)) tooltip.add(Component.translatable(desc3Key).withStyle(ChatFormatting.GRAY));
    }
} 
