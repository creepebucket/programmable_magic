package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaintDataSpell extends BaseSpellEffectLogic {

    @Override
    public String getRegistryName() {
        return "paint_data";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        if (player == null || data == null) return true;

        java.util.List<Component> lines = new java.util.ArrayList<>();
        lines.add(Component.literal("==== SpellData ====").withStyle(ChatFormatting.GOLD));

        String pos = String.format(java.util.Locale.ROOT, "(%.2f, %.2f, %.2f)", data.getPosition().x, data.getPosition().y, data.getPosition().z);
        String dir = String.format(java.util.Locale.ROOT, "(%.2f, %.2f, %.2f)", data.getDirection().x, data.getDirection().y, data.getDirection().z);
        Entity target = data.getTarget();
        String targetInfo = target == null ? "null" : target.getClass().getSimpleName() + "#" + target.getId();

        lines.add(Component.literal(String.format(java.util.Locale.ROOT,
                "Power %.2f  Range %.1f  Delay %d  Active %s",
                data.getPower(), data.getRange(), data.getDelay(), data.isActive()))
                .withStyle(ChatFormatting.YELLOW));
        lines.add(Component.literal("Pos " + pos + "  Dir " + dir).withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Target " + targetInfo).withStyle(ChatFormatting.DARK_AQUA));

        Map<String, Double> mana = data.getAllManaCosts();
        lines.add(Component.literal(String.format(java.util.Locale.ROOT,
                "Mana  Rad %s  Tmp %s  Mom %s  Pre %s",
                ModUtils.FormattedManaString(mana.getOrDefault("radiation", 0.0)),
                ModUtils.FormattedManaString(mana.getOrDefault("temperature", 0.0)),
                ModUtils.FormattedManaString(mana.getOrDefault("momentum", 0.0)),
                ModUtils.FormattedManaString(mana.getOrDefault("pressure", 0.0))))
                .withStyle(ChatFormatting.RED));

        Map<String, Object> custom = data.getAllCustomData();
        if (custom.isEmpty()) {
            lines.add(Component.literal("Custom <empty>").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.literal("Custom").withStyle(ChatFormatting.GRAY));
            int shown = 0;
            for (Map.Entry<String, Object> entry : custom.entrySet()) {
                if (shown >= 6) {
                    lines.add(Component.literal("  ...").withStyle(ChatFormatting.DARK_GRAY));
                    break;
                }
                lines.add(Component.literal("  " + entry.getKey() + ": " + String.valueOf(entry.getValue()))
                        .withStyle(ChatFormatting.WHITE));
                shown++;
            }
        }

        List<?> runtimeSeq = data.getCustomData("__seq", List.class);
        if (runtimeSeq instanceof List<?> list && !list.isEmpty()) {
            lines.addAll(buildSequenceDump(data, list));
        } else {
            List<?> cached = data.getCustomData("__debug_sequence", List.class);
            if (cached != null && !cached.isEmpty()) {
                lines.add(Component.literal("Sequence").withStyle(ChatFormatting.DARK_BLUE));
                int shown = 0;
                for (Object entry : cached) {
                    if (shown >= 20) {
                        lines.add(Component.literal("  ...").withStyle(ChatFormatting.DARK_GRAY));
                        break;
                    }
                    lines.add(Component.literal("  " + String.valueOf(entry)).withStyle(ChatFormatting.WHITE));
                    shown++;
                }
            }
        }

        for (Component line : lines) {
            player.displayClientMessage(line, false);
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
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = new java.util.ArrayList<>();
        in.add(org.creepebucket.programmable_magic.spells.SpellValueType.MODIFIER);
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.SPELL;
        var desc = net.minecraft.network.chat.Component.literal("打印 SpellData 以调试");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(in, out, desc, this);
    }

    private List<Component> buildSequenceDump(SpellData data, List<?> sequence) {
        List<Component> dump = new ArrayList<>();
        dump.add(Component.literal("Sequence").withStyle(ChatFormatting.DARK_BLUE));
        int shown = 0;
        for (int i = 0; i < sequence.size(); i++) {
            if (shown >= 24) { dump.add(Component.literal("  ...").withStyle(ChatFormatting.DARK_GRAY)); break; }
            Object obj = sequence.get(i);
            if (obj instanceof SpellItemLogic logic) {
                String line = formatEntry(data, i, logic);
                dump.add(Component.literal("  " + line).withStyle(ChatFormatting.WHITE));
            } else {
                dump.add(Component.literal("  " + String.format(Locale.ROOT, "%02d | %s", i, String.valueOf(obj)))
                        .withStyle(ChatFormatting.WHITE));
            }
            shown++;
        }
        return dump;
    }

    private String formatEntry(SpellData data, int index, SpellItemLogic logic) {
        String name = logic != null && logic.getRegistryName() != null ? logic.getRegistryName() : "<null>";
        String type = logic != null ? logic.getSpellType().name() : "?";
        ComputeValue value = data.getComputeValue(index);
        String extra = value != null ? formatValue(value) : "";
        return String.format(Locale.ROOT, "%02d | %s [%s]%s", index, name, type, extra);
    }

    private String formatValue(ComputeValue value) {
        if (value == null || value.value() == null) return "";
        return switch (value.type()) {
            case NUMBER -> {
                double num = value.value() instanceof Number n ? n.doubleValue() : 0.0;
                yield " -> " + String.format(Locale.ROOT, "%.4f", num);
            }
            case VECTOR3 -> {
                var v = value.value();
                String text = v instanceof net.minecraft.world.phys.Vec3 vec
                        ? String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z)
                        : String.valueOf(v);
                yield " -> " + text;
            }
            case ENTITY -> " -> " + value.value().toString();
            default -> " -> " + value.value();
        };
    }
}
