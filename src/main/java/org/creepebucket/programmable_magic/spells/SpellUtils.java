package org.creepebucket.programmable_magic.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.adjust_mod.BaseAdjustModLogic;
import org.creepebucket.programmable_magic.spells.base_spell.BaseBaseSpellLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.MathOpreationsSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.ParenSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.SpellSeperator;
import org.creepebucket.programmable_magic.spells.compute_mod.ValueLiteralSpell;
import org.creepebucket.programmable_magic.spells.control_mod.BaseControlModLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.creepebucket.programmable_magic.ModUtils.sendErrorMessageToPlayer;

/**
 * 执行法术相关的工具函数。
 * 将原先在 SpellEntity#tick 中的法术执行与参数收集逻辑迁移到此处，避免实体类过于臃肿。
 */
public final class SpellUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellUtils");

    private SpellUtils() {}

    public static class StepResult {
        public final boolean shouldDiscard;
        public final boolean successful;
        public final int delayTicks;
        public final Map<String, Object> result;

        public StepResult(boolean shouldDiscard, boolean successful, int delayTicks, Map<String, Object> result) {
            this.shouldDiscard = shouldDiscard;
            this.successful = successful;
            this.delayTicks = delayTicks;
            this.result = result;
        }
    }

    /**
     * 执行当前索引的法术一步，并返回执行结果。
     */
    public static StepResult executeCurrentSpell(Player caster,
                                                 SpellData spellData,
                                                 SpellSequence sequence,
                                                 SpellItemLogic currentSpell) {

        if (currentSpell == null) return new StepResult(true, false, 0,  Map.of());

        // 收集法术参数和修饰法术
        List<SpellItemLogic> modifiers = new ArrayList<>();
        List<Object> spellParams = new ArrayList<>();

        if (currentSpell instanceof BaseBaseSpellLogic) {
            // 向左寻找修饰, 直到遇到上一个基础法术
            for (SpellItemLogic p = currentSpell.getPrevSpell(); p != null; p = p.getPrevSpell()) {
                if (p instanceof BaseBaseSpellLogic) break;
                if (p instanceof BaseAdjustModLogic || p instanceof BaseControlModLogic) {
                    modifiers.add(p);
                }
            }
        }

        List<List<SpellValueType>> neededParamsType = currentSpell.getNeededParamsType();

        // 对于每个法术的重载, 都需要尝试匹配 FUCK
        for (int k = 0; k < neededParamsType.size(); k++ ) {
            // 空参数特判
            if (Objects.equals(neededParamsType.get(k), List.of(SpellValueType.EMPTY))) { continue; }

            int total = neededParamsType.get(k).size();
            int right = Math.max(0, Math.min(currentSpell.RightParamsOffset, total));
            int left = total - right;

            // 先从左侧收集参数（远端到近端的顺序，确保与 neededParamsType 对齐）
            if (left > 0) {
                SpellItemLogic p = currentSpell;
                // 移动到左侧最远那个参数位置
                for (int i = 0; i < left; i++) {
                    p = (p == null) ? null : p.getPrevSpell();
                }
                for (int i = 0; i < left; i++) {
                    if (p == null) return paramOutOfBound(caster, currentSpell);
                    if (!(p instanceof ValueLiteralSpell v)) return paramTypeError(caster, currentSpell);
                    Object neededType = neededParamsType.get(k).get(i);
                    if (!v.VALUE_TYPE.equals(neededType) && !neededType.equals(SpellValueType.ANY)) {
                        return paramTypeMismatch(caster, currentSpell, v, (SpellValueType) neededType);
                    }
                    spellParams.add(v.VALUE);
                    p = p.getNextSpell(); // 逐个向右靠近当前法术
                }
            }

            // 再从右侧收集参数（从近到远，追加到末尾）
            if (right > 0) {
                SpellItemLogic n = currentSpell.getNextSpell();
                for (int j = 0; j < right; j++) {
                    if (n == null) return paramOutOfBound(caster, currentSpell);
                    if (!(n instanceof ValueLiteralSpell v)) return paramTypeError(caster, currentSpell);
                    Object neededType = neededParamsType.get(k).get(left + j);
                    if (!v.VALUE_TYPE.equals(neededType) && !neededType.equals(SpellValueType.ANY)) {
                        return paramTypeMismatch(caster, currentSpell, v, (SpellValueType) neededType);
                    }
                    spellParams.add(v.VALUE);
                    n = n.getNextSpell();
                }
            }
        }

        Map<String, Object> result = currentSpell.run(caster, spellData, sequence, modifiers, spellParams);

        int delayTicks = 0;
        if (result.containsKey("delay")) {
            try { delayTicks = (int) result.get("delay"); } catch (Exception ignored) { delayTicks = 0; }
        }
        boolean successful = false;
        try { successful = Boolean.TRUE.equals(result.get("successful")); } catch (Exception ignored) { successful = false; }

        return new StepResult(false, successful, delayTicks, result);
    }

    // 简单的按索引取节点（从 head 线性前进）
    private static SpellItemLogic at(SpellSequence seq, int index) {
        int i = 0;
        for (SpellItemLogic it = seq.getFirstSpell(); it != null; it = it.getNextSpell()) {
            if (i == index) return it;
            i++;
        }
        return null;
    }

    private static StepResult paramOutOfBound(Player caster, SpellItemLogic current) {
        LOGGER.error("在搜索参数时突破边界: 当前法术: {}", current.getRegistryName());
        sendErrorMessageToPlayer(Component.translatable("message.programmable_magic.error.wand.param_search_out_of_bound"), caster);
        return new StepResult(true, false, 0, Map.of());
    }

    private static StepResult paramTypeError(Player caster, SpellItemLogic current) {
        LOGGER.error("尝试收集参数时发现参数不是ValueLiteralSpell类型: 当前法术: {}", current.getRegistryName());
        sendErrorMessageToPlayer(Component.translatable("message.programmable_magic.error.wand.internal_bug"), caster);
        return new StepResult(true, false, 0, Map.of());
    }

    private static StepResult paramTypeMismatch(Player caster, SpellItemLogic current, ValueLiteralSpell v, SpellValueType neededType) {
        LOGGER.error("尝试收集参数时发现参数类型错误: 当前法术: {} 参数类型: {} 需要的类型: {}",
                current.getRegistryName(), v.VALUE_TYPE, neededType);
        sendErrorMessageToPlayer(Component.translatable(
                "message.programmable_magic.error.wand.param_type_error",
                current.getRegistryName(), v.VALUE_TYPE.display(), neededType.display()
        ), caster);
        return new StepResult(true, false, 0, Map.of());
    }


    public static SpellSequence calculateSpellSequence(Player player, SpellData spellData, SpellSequence seq) {
        LOGGER.debug("开始计算法术序列");

        // 先寻找括号, 对每个括号内的部分进行递归计算
        List<List<SpellItemLogic>> pairs = getParenPairs(player, spellData, seq);
        for (List<SpellItemLogic> pair : pairs) {
            if (pair == null || pair.size() != 2) continue;
            SpellItemLogic left = pair.get(0);
            SpellItemLogic right = pair.get(1);

            // 抽取区间 (left, right) 形成子序列
            SpellSequence inner = seq.subSequence(left, right);

            // 递归计算子序列
            inner = calculateSpellSequence(player, spellData, inner);

            // 用递归结果替换整段 [left..right]
            seq.replaceSection(left, right, inner);
        }

        // 若仍存在未消解的括号，递归继续处理，确保算符阶段前无括号残留
        if (!getParenPairs(player, spellData, seq).isEmpty()) {
            return calculateSpellSequence(player, spellData, seq);
        }

        // 再按计算顺序进行计算
        final List<MathOpreationsSpell> ORDER = List.of(
                new MathOpreationsSpell.PowerSpell(),
                new MathOpreationsSpell.MultiplicationSpell(),
                new MathOpreationsSpell.DivisionSpell(),
                new MathOpreationsSpell.AdditionSpell(),
                new MathOpreationsSpell.SubtractionSpell()
        );

        for (SpellItemLogic operator : ORDER) {
            // 查找到每个运算符执行对应的run并替换区间

            SpellItemLogic current = seq.getFirstSpell();
            while (current != null) {
                if (current.getClass() == operator.getClass()) {
                    SpellItemLogic L = current.getPrevSpell();
                    SpellItemLogic R = current.getNextSpell();
                    SpellItemLogic nextAfter = (R != null) ? R.getNextSpell() : null;

                    // 检测两边是否都是ValueLiteralSpell
                    if (L instanceof ValueLiteralSpell && R instanceof ValueLiteralSpell) {
                        Map<String, Object> result = operator.run(player, spellData, seq, null, List.of(
                                ((ValueLiteralSpell) L).VALUE, ((ValueLiteralSpell) R).VALUE));

                        seq.replaceSection(L, R, new SpellSequence(List.of(new ValueLiteralSpell((SpellValueType) result.get("type"), result.get("value")))));
                    }

                    current = nextAfter;
                    continue;
                }
                current = current.getNextSpell();
            }
        }

        boolean flag = false;
        // 遍历每个法术, 执行剩下的COMPUTE_MOD

        for (SpellItemLogic spell = seq.getFirstSpell(); spell != null; spell = spell.getNextSpell()) {
            if (!(spell.getSpellType() == SpellItemLogic.SpellType.COMPUTE_MOD) || spell instanceof ValueLiteralSpell || spell instanceof MathOpreationsSpell) continue;

            var step = SpellUtils.executeCurrentSpell(player, spellData, seq, spell);
            if (!step.successful) continue;
            Map<String, Object> result = step.result;

            SpellItemLogic L = spell;
            SpellItemLogic R = spell;

            if (!Objects.equals(spell.getNeededParamsType().get(0), List.of(SpellValueType.EMPTY))) { // EMPTY特判
                for (int i = 0; i < spell.getNeededParamsType().get(0).size() - spell.RightParamsOffset; i++) {
                    L = L.getPrevSpell();
                }

                for (int i = 0; i < spell.RightParamsOffset; i++) {
                    R = R.getNextSpell();
                }
            }

            seq.replaceSection(L, R, new SpellSequence(List.of(new ValueLiteralSpell((SpellValueType) result.get("type"), result.get("value")))));
            flag = true;

            // TODO: 检查法术参数数量不同的重载
        }


        // 如果计算完成后, 还有剩余COMPUTE_MOD残留, 再次递归调用calculateSpellSequence确保计算干净
        if (flag) return calculateSpellSequence(player, spellData, seq);

        LOGGER.debug("法术序列计算完成");
        return seq;
    }

    public static List<SpellItemLogic> getSeps(SpellSequence spellSequence) {
        List<SpellItemLogic> seps = new ArrayList<>();
        for (SpellItemLogic it = spellSequence.getFirstSpell(); it != null; it = it.getNextSpell()) { if (it instanceof SpellSeperator) { seps.add(it);} }
        return seps;
    }

    private static List<List<SpellItemLogic>> getParenPairs(Player player, SpellData spellData, SpellSequence spellSequence) {
        SpellItemLogic i = spellSequence.getFirstSpell();
        List<List<SpellItemLogic>> pairs = new ArrayList<>();
        while (i != null) {
            if (i instanceof ParenSpell.LeftParenSpell) {
                Object right = i.run(player, spellData, spellSequence, null, null).get("value");
                if (right instanceof SpellItemLogic r) {
                    pairs.add(List.of(i, r));
                }
            }
            i = i.getNextSpell();
        }
        return pairs;
    }
}
