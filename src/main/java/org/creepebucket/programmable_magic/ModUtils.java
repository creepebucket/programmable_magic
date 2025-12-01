package org.creepebucket.programmable_magic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModUtils {
    public static List<ItemStack> getItemsFromTag(TagKey<Item> tagKey) {
        List<ItemStack> items = new ArrayList<>();

        BuiltInRegistries.ITEM.stream().filter(item -> {try {
            return item.getDefaultInstance().is(tagKey);
        } catch (Exception e) {return false;}
        }).forEach(item -> {try{
            items.add(new ItemStack(item));} catch (Exception e) {}
        });

        return items;
    }

    // 格式化魔力（1 点 mana = 1 KJ），保留约 4 位有效数字
    public static String FormattedManaString(double mana) {
        String[] units = {"J", "KJ", "MJ", "GJ", "TJ", "PJ", "EJ", "ZJ", "YJ"};

        // 将 mana 点数转为焦耳（J）
        double joules = mana * 1000.0;

        // 选择合适单位（1000 进制）
        int idx = 0;
        while (Math.abs(joules) >= 1000.0 && idx < units.length - 1) {
            joules /= 1000.0;
            idx++;
        }

        // 计算保留小数位数以达到约 4 位有效数字
        double absVal = Math.abs(joules);
        int decimals;
        if (absVal == 0.0) {
            decimals = 0;
        } else if (absVal >= 1.0) {
            int intDigits = (int) Math.floor(Math.log10(absVal)) + 1; // 1..3
            decimals = Math.max(0, 4 - intDigits);
        } else {
            // 小于 1 的情况，直接保留 4 位小数（再去除末尾 0）
            decimals = 4;
        }

        String num = String.format("%." + decimals + "f", joules);
        // 去除多余的末尾 0 和小数点
        if (num.indexOf('.') >= 0) {
            while (num.endsWith("0")) num = num.substring(0, num.length() - 1);
            if (num.endsWith(".")) num = num.substring(0, num.length() - 1);
        }

        return num + " " + units[idx];
    }

    public static boolean sendErrorMessageToPlayer(Component message, Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(message);
            return true;
        }
        return false;
    }
}
