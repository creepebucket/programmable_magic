package org.creepebucket.programmable_magic.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpellTooltipUtil {
    private SpellTooltipUtil() {}

    private static final Map<String, ChatFormatting> MANA_COLOR = Map.of(
            "radiation", ChatFormatting.YELLOW,
            "temperature", ChatFormatting.RED,
            "momentum", ChatFormatting.BLUE,
            "pressure", ChatFormatting.GREEN
    );

    public static List<Component> buildTooltip(List<SpellValueType> inputs, SpellValueType output, Component description,
                                               SpellItemLogic logic) {
        List<Component> t = new ArrayList<>();
        t.add(signature(inputs, output));
        if (description != null) t.add(description);
        t.add(Component.translatable("tooltip.programmable_magic.mana_cost"));
        Map<String, Double> costs = baseCosts(logic);
        t.add(joinManaCosts(costs));
        return t;
    }

    public static Component signature(List<SpellValueType> inputs, SpellValueType output) {
        MutableComponent c = Component.literal("(");
        for (int i = 0; i < inputs.size(); i++) {
            if (i > 0) c.append(Component.literal(", "));
            c.append(inputs.get(i).typed());
        }
        c.append(Component.literal(") -> ("));
        c.append(output.typed());
        c.append(Component.literal(")"));
        return c;
    }

    public static Component joinManaCosts(Map<String, Double> costs) {
        MutableComponent line = Component.empty();
        appendOne(line, "radiation", costs);
        line.append(Component.literal("  "));
        appendOne(line, "momentum", costs);
        line.append(Component.literal("  "));
        appendOne(line, "pressure", costs);
        line.append(Component.literal("  "));
        appendOne(line, "temperature", costs);
        return line;
    }

    private static void appendOne(MutableComponent line, String type, Map<String, Double> costs) {
        ChatFormatting color = MANA_COLOR.getOrDefault(type, ChatFormatting.WHITE);
        String shortName = switch (type) {
            case "radiation" -> "Rad";
            case "momentum" -> "Mom";
            case "pressure" -> "Pre";
            case "temperature" -> "Tmp";
            default -> type;
        };
        double v = costs.getOrDefault(type, 0.0);
        line.append(Component.literal(shortName + ": ").withStyle(color))
                .append(Component.literal(ModUtils.FormattedManaString(v)).withStyle(color));
    }

    public static Map<String, Double> baseCosts(SpellItemLogic logic) {
        // 以一次独立的 SpellData 计算基础耗魔（无需构造完整参数序列）
        SpellData d = new SpellData(null, Vec3.ZERO, new Vec3(0, 0, 1));
        try {
            logic.calculateBaseMana(d);
            logic.applyManaModification(d);
        } catch (Exception ignored) {}
        Map<String, Double> m = new HashMap<>();
        m.put("radiation", d.getManaCost("radiation"));
        m.put("temperature", d.getManaCost("temperature"));
        m.put("momentum", d.getManaCost("momentum"));
        m.put("pressure", d.getManaCost("pressure"));
        return m;
    }
}
