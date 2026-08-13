package org.creepebucket.arcanism.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Climate;
import org.creepebucket.arcanism.items.BaseSpellItem;
import org.creepebucket.arcanism.registries.WandPluginRegistry;
import org.creepebucket.arcanism.spells.plugins.WandPluginLogic;

import java.util.*;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModUtils {
    public static List<ItemStack> getItemsFromTag(TagKey<Item> tagKey) {
        List<ItemStack> items = new ArrayList<>();

        BuiltInRegistries.ITEM.stream().filter(item -> {
            try {
                return item.getDefaultInstance().is(tagKey);
            } catch (Exception e) {
                return false;
            }
        }).forEach(item -> {
            try {
                items.add(new ItemStack(item));
            } catch (Exception e) {
            }
        });

        return items;
    }

    // 格式化魔力（1 点 availableMana = 1 J），保留约 4 位有效数字
    public static String FormattedManaString(double mana) {
        return formattedNumber(mana) + "J";
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

    // 基础单位转换（1000 进制），按显示位数动态决定小数位数，不省略末尾 0
    public static String formattedNumber(double value, int stringLength) {
        String[] prefixes = new String[]{"", "K", "M", "G", "T", "P", "E", "Z", "Y"};
        int exp = (int) Math.floor(Math.log10(value));

        try {
            if (Double.isInfinite(value)) {
                return "∞" + " ".repeat(stringLength - 1);
            } else if (value < 10) {
                // 不可以log的情况
                return String.format("%." + (stringLength - 3) + "f", value) + " ";
            } else if (value >= 1e27) {
                // 使用科学计数法
                var man = value / Math.pow(10, exp);

                int expDecimals = exp <= 1 ? 1 : (int) Math.ceil(Math.log10(exp));
                return String.format("%." + (stringLength - 4 - expDecimals) + "f", man) + "e" + exp + " ";
            } else if (value >= 1000) {
                // 使用前缀
                int index = (int) Math.floor((double) exp / 3);
                var man = value / Math.pow(10, index * 3);
                int manDecimals = man <= 1 ? 1 : (int) Math.floor(Math.log10(man)) + 1;

                return String.format("%." + (stringLength - 3 - manDecimals) + "f", man) + " " + prefixes[index];
            } else {
                // 直接输出
                int manDecimals = value <= 1 ? 1 : (int) Math.floor(Math.log10(value)) + 1;
                return String.format("%." + (stringLength - 2 - manDecimals) + "f", value) + " ";
            }
        } catch (UnknownFormatConversionException _) {
            System.out.println("prm数字转换出错: " + value + " " + stringLength);
            return "bug了请报告"; // 实在没招了, 有bug
        }
    }

    public static boolean sendErrorMessageToPlayer(Component message, Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(message);
            return true;
        }
        return false;
    }

    public static Component formatSpellError(Component kind, Component detail) {
        return Component.translatable("message.arcanism.error.spell_error_header")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(" "))
                .append(kind.copy().withStyle(ChatFormatting.GOLD))
                .append(Component.literal("\n"))
                .append(detail.copy().withStyle(ChatFormatting.GRAY));
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
            WandPluginLogic plugin = WandPluginRegistry.getPlugin(item);
            if (plugin == null) continue;
            plugin.adjustWandValues(values, st);
        }
        return values;
    }

    /**
     * 用于法术序列化的 id -> 名称映射表.
     * 将法术id字符串替换为一种接近代码的格式
     *
     * @return 映射表, k=原id, v=映射后
     */
    public static Map<String, String> getSpellSerializeMap() {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("number_digit_0", "0");
        map.put("number_digit_1", "1");
        map.put("number_digit_2", "2");
        map.put("number_digit_3", "3");
        map.put("number_digit_4", "4");
        map.put("number_digit_5", "5");
        map.put("number_digit_6", "6");
        map.put("number_digit_7", "7");
        map.put("number_digit_8", "8");
        map.put("number_digit_9", "9");
        map.put("addition", "+");
        map.put("subtraction", "-");
        map.put("multiplication", "*");
        map.put("division", "/");
        map.put("remainder", "%");
        map.put("exponent", "^");
        map.put("vector_length", "length_of");
        map.put("vector_x", "x_of");
        map.put("vector_y", "y_of");
        map.put("vector_z", "z_of");
        map.put("entity_armor", "armor_of");
        map.put("entity_health", "hp_of");
        map.put("entity_max_health", "max_hp_of");
        map.put("block_position", "block_of");
        map.put("l_paren", "(");
        map.put("r_paren", ")");
        map.put("comma", ",");
        map.put("entity_position", "position_of");
        map.put("entity_velocity", "velocity_of");
        map.put("greater_than", ">");
        map.put("less_than", "<");
        map.put("equal_to", "==");
        map.put("greater_equal_to", ">=");
        map.put("less_equal_to", "<=");
        map.put("not_equal_to", "!=");
        map.put("and", "&");
        map.put("or", "|");
        map.put("not", "!");
        map.put("block_is_air", "is_air?");
        map.put("block_has_gravity", "has_gravity?");
        map.put("loop_start", "\nloop{\n");
        map.put("for_loop", "\nfor{\n");
        map.put("loop_end", "\n}\n");
        map.put("if_start", "if_true_do[\n");
        map.put("if_end", "]\n");
        map.put("condition_invert", "inverted");
        map.put("touch_ground", "wait_until_touch_ground");
        map.put("touch_entity", "wait_until_touch_entity");
        map.put("debug_print", "print");
        map.put("mine_block", "mine_block");

        return map;
    }

    public static String serializeSpells(Container spell, boolean compactMode) {
        // 取出每个法术(只序列化法术, 不要序列化其他物品和空格) 提取name属性 然后映射
        Map<String, String> map = getSpellSerializeMap();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < spell.getContainerSize(); i++) {
            ItemStack stack = spell.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BaseSpellItem spellItem)) continue;
            String logicName = spellItem.getLogic().name;
            String mapped = map.get(logicName);
            if (mapped == null || mapped.isEmpty()) mapped = logicName;
            stringBuilder.append(mapped.replace(compactMode ? "\n" : "", "")).append(!compactMode ? " " : "");
        }
        return stringBuilder.toString();
    }

    public static Container deSerializeSpells(String string) {
        // 丢弃所有空格, 再反查映射
        Map<String, String> map = getSpellSerializeMap();
        Map<String, String> reverseMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) reverseMap.put(entry.getValue().replace("\n", ""), entry.getKey());
        BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BaseSpellItem)
                .forEach(item -> reverseMap.putIfAbsent(((BaseSpellItem) item).getLogic().name, ((BaseSpellItem) item).getLogic().name));

        String input = string.replace(" ", "").replace("\n", "");
        Container container = new SimpleContainer(1024);

        int index = 0;
        int slot = 0;
        while (index < input.length()) {
            String matched = null;
            for (String token : reverseMap.keySet()) {
                if (!input.startsWith(token, index)) continue;
                if (matched == null || token.length() > matched.length()) matched = token;
            }
            if (matched == null) {
                index++;
                continue;
            }

            String logicName = reverseMap.getOrDefault(matched, "");
            var itemHolder = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath(MODID, "spell_display_" + logicName)).orElse(null);
            if (itemHolder != null) {
                // 越界检查
                if (slot == 1024) return container;

                container.setItem(slot, new ItemStack(itemHolder.value()));
                slot++;
            }

            index += matched.length();
        }

        return container;
    }

    public static double now() {
        return System.nanoTime() / 1e9;
    }

    /**
     * 插件数值聚合对象：所有字段默认 0，由插件按需调整。
     */
    public static class WandValues {
        public double manaMult = 0.0;   // 魔力倍率：0 表示无倍率（按现有计算规则处理）
        public double chargeRateW = 0.0; // 充能功率（W）
        public int spellSlots = 0;       // 法术槽位有效容量
        public int pluginSlots = 0;      // 插件槽有效容量（当前未使用）
    }

    /**
     * 包含端点
     */
    public static int simpleRandInt(int a, int b) {
        if (a > b) return simpleRandInt(b, a);
        return (int) (Math.floor(Math.random() * (b - a + 1)) + a);
    }

    public static double getTempKelvin(BlockPos pos, ServerLevel level) {
        var climate = level.getChunkSource().randomState().sampler().sample(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ()));
        var noiseTemp = Climate.unquantizeCoord(climate.temperature());
        return noiseTemp * 50 + 273.15;
    }
}
