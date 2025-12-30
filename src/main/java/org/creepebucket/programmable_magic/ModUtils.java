package org.creepebucket.programmable_magic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModUtils {
    /**
     * 插件数值聚合对象：所有字段默认 0，由插件按需调整。
     */
    public static class WandValues {
        public double manaMult = 0.0;   // 魔力倍率：0 表示无倍率（按现有计算规则处理）
        public double chargeRateW = 0.0; // 充能功率（W）
        public int spellSlots = 0;       // 法术槽位有效容量
        public int pluginSlots = 0;      // 插件槽有效容量（当前未使用）
    }
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
        double joules = mana * 1000.0; // 转为焦耳
        return formattedNumber(joules) + "J";
    }

    // 基础单位转换（1000 进制），不省略末尾 0
    public static String formattedNumber(double value) {
        String[] prefixes = new String[]{"", "K", "M", "G", "T", "P", "E", "Z", "Y"};

        double v = value;
        int idx = 0;
        while (Math.abs(v) >= 1000.0 && idx < prefixes.length - 1) {
            v /= 1000.0;
            idx++;
        }

        double absVal = Math.abs(v);
        int decimals;
        if (absVal == 0.0) {
            decimals = 0;
        } else if (absVal >= 1.0) {
            int intDigits = (int) Math.floor(Math.log10(absVal)) + 1; // 1..3
            decimals = Math.max(0, 4 - intDigits);
        } else {
            decimals = 4;
        }

        return String.format("%." + decimals + "f %s", v, prefixes[idx]);
    }

    public static boolean sendErrorMessageToPlayer(Component message, Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(message);
            return true;
        }
        return false;
    }

    /**
     * 由插件物品聚合魔杖数值：
     * - 遍历每个插件物品，构造插件实例并调用其 adjustWandValues。
     * - 返回聚合后的 WandValues（默认各字段为 0）。
     */
    public static WandValues computeWandValues(List<ItemStack> stacks) {
        WandValues values = new WandValues();
        if (stacks == null) return values;
        for (ItemStack st : stacks) {
            if (st == null || st.isEmpty()) continue;
            Item item = st.getItem();
            BasePlugin plugin = WandPluginRegistry.createPlugin(item);
            if (plugin == null) continue;
            plugin.adjustWandValues(values, st);
        }
        return values;
    }

    public static final class Mana {

        public static final String RADIATION = "radiation";
        public static final String TEMPERATURE = "temperature";
        public static final String MOMENTUM = "momentum";
        public static final String PRESSURE = "pressure";

        private final Map<String, Double> values;

        public Mana(Double radiation, Double temperature, Double momentum, Double pressure) {
            this.values = new HashMap<>();
            // 预置四系键，初始为 0.0
            values.put(RADIATION, radiation);
            values.put(TEMPERATURE, temperature);
            values.put(MOMENTUM, momentum);
            values.put(PRESSURE, pressure);
        }

        public Mana() {
            this(0.0, 0.0, 0.0, 0.0);
        }
    
        public Map<String, Double> toMap() {
            return values;
        }

        public void add(String key, Double value) {
            values.put(key, values.get(key) + value);
        }

        public void add(Mana mana) {
            for (Map.Entry<String, Double> entry : mana.toMap().entrySet()) {
                values.put(entry.getKey(), values.get(entry.getKey()) + entry.getValue());
            }
        }

        public Double getRadiation() {
            return values.get(RADIATION);
        }
        public Double getTemperature() {
            return values.get(TEMPERATURE);
        }
        public Double getMomentum() {
            return values.get(MOMENTUM);
        }
        public Double getPressure() {
            return values.get(PRESSURE);
        }

        public Mana negative() {
            Mana mana = new Mana();
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                mana.add(entry.getKey(), -entry.getValue());
            }
            return mana;
        }

        public boolean greaterThan(Mana mana) {
            return values.get(RADIATION) > mana.getRadiation() &&
                    values.get(TEMPERATURE) > mana.getTemperature() &&
                    values.get(MOMENTUM) > mana.getMomentum() &&
                    values.get(PRESSURE) > mana.getPressure();
        }

        public boolean lessThan(Mana mana) {
            return values.get(RADIATION) < mana.getRadiation() &&
                    values.get(TEMPERATURE) < mana.getTemperature() &&
                    values.get(MOMENTUM) < mana.getMomentum() &&
                    values.get(PRESSURE) < mana.getPressure();
        }

        // 任一分量大于即返回真：用于“是否有任一系魔力不足”的判定
        public boolean anyGreaterThan(Mana mana) {
            return values.get(RADIATION) > mana.getRadiation() ||
                    values.get(TEMPERATURE) > mana.getTemperature() ||
                    values.get(MOMENTUM) > mana.getMomentum() ||
                    values.get(PRESSURE) > mana.getPressure();
        }
    }
}
