package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.*;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleComputeSpell extends BaseComputeModLogic {

    @Override
    public boolean run(Player player, SpellData data) {
        // 缺省无行为（由具体实现或预处理生成的 NumberLiteralSpell 覆盖）
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        // 计算类法术作为修饰符参与计费，基础魔力不在此处计入
    }

    @Override
    public void applyManaModification(SpellData data) {
        // 计算类法术每个实例叠加固定耗魔：四系各 0.005
        data.addManaCost("radiation", 0.005);
        data.addManaCost("temperature", 0.005);
        data.addManaCost("momentum", 0.005);
        data.addManaCost("pressure", 0.005);
    }

    @Override
    public List<Component> getTooltip() {
        String rn = getRegistryName();
        List<SpellValueType> in = new ArrayList<>();
        SpellValueType out = SpellValueType.NUMBER;
        Component desc;
        if (rn != null && rn.startsWith("compute_") && rn.length() > 8) {
            char c = rn.charAt(rn.length() - 1);
            if (c >= '0' && c <= '9') {
                // 数字
                desc = Component.literal("数字");
                return SpellTooltipUtil.buildTooltip(in, out, desc, this);
            }
        }
        switch (rn) {
            case "compute_add": case "compute_sub": case "compute_mul": case "compute_div": case "compute_pow":
                in.add(SpellValueType.NUMBER); in.add(SpellValueType.NUMBER);
                desc = Component.literal("数学运算");
                break;
            case "compute_lparen": case "compute_rparen":
                in.add(SpellValueType.NUMBER);
                desc = Component.literal("表达式分组");
                break;
            default:
                in.add(SpellValueType.NUMBER);
                desc = Component.literal("计算组件");
        }
        return SpellTooltipUtil.buildTooltip(in, out, desc, this);
    }
}
