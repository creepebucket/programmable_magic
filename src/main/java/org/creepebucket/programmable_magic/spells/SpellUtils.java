package org.creepebucket.programmable_magic.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.adjust_mod.BaseAdjustModLogic;
import org.creepebucket.programmable_magic.spells.base_spell.BaseBaseSpellLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.MathOperationsSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.ParenSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.SpellSeperator;
import org.creepebucket.programmable_magic.spells.compute_mod.ValueLiteralSpell;
import org.creepebucket.programmable_magic.spells.control_mod.BaseControlModLogic;
import org.creepebucket.programmable_magic.spells.control_mod.BlockConditionSpell;
import org.creepebucket.programmable_magic.spells.control_mod.LogicalOperationsSpell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.creepebucket.programmable_magic.ModUtils.sendErrorMessageToPlayer;
import static org.creepebucket.programmable_magic.ModUtils.formatSpellError;

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
        public final ModUtils.Mana mana;

        public StepResult(boolean shouldDiscard, boolean successful, int delayTicks, Map<String, Object> result, ModUtils.Mana mana) {
            this.shouldDiscard = shouldDiscard;
            this.successful = successful;
            this.delayTicks = delayTicks;
            this.result = result;
            this.mana = mana;
        }
    }

    /**
     * 从 SpellEntity 携带的插件物品构造插件实例列表（服务端）。
     */
    public static java.util.List<BasePlugin> getInstalledPlugins(SpellEntity entity) {
        java.util.ArrayList<BasePlugin> list = new java.util.ArrayList<>();
        if (entity == null) return list;
        java.util.List<ItemStack> stacks = entity.getPluginItems();
        if (stacks == null) return list;
        for (ItemStack st : stacks) {
            if (st == null || st.isEmpty()) continue;
            var p = WandPluginRegistry.createPlugin(st.getItem());
            if (p != null) list.add(p);
        }
        return list;
    }

    

    /**
     * 执行当前索引的法术一步，并返回执行结果。
     */
    public static StepResult executeCurrentSpell(Player caster,
                                                 SpellData spellData,
                                                 SpellSequence sequence,
                                                 SpellItemLogic currentSpell,
                                                 ModUtils.Mana myMana) {

        if (currentSpell == null) return new StepResult(true, false, 0,  Map.of(), new ModUtils.Mana());

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

        // 按重载顺序逐一尝试，命中一个即采用并停止
        List<Object> matchedParams = null;
        boolean matched = false;
        ValueLiteralSpell firstMismatchV = null;
        SpellValueType firstNeededType = null;
        boolean seenOutOfBound = false;
        boolean seenNonLiteral = false;

        // 无重载定义，视为无需参数
        if (neededParamsType == null || neededParamsType.isEmpty()) {
            matched = true;
            matchedParams = List.of();
        }

        // 对于每个法术的重载, 都需要尝试匹配 FUCK
        for (int k = 0; !matched && k < neededParamsType.size(); k++) {
            List<SpellValueType> types = neededParamsType.get(k);

            // 空参数重载：直接命中
            if (Objects.equals(types, List.of(SpellValueType.EMPTY))) {
                matchedParams = List.of();
                matched = true;
                break;
            }

            int total = types.size();
            int right = Math.max(0, Math.min(currentSpell.RightParamsOffset, total));
            int left = total - right;

            boolean ok = true;
            List<Object> attemptParams = new ArrayList<>();

            // 先从左侧收集参数（远端到近端）
            if (left > 0) {
                SpellItemLogic p = currentSpell;
                for (int i = 0; i < left; i++) { p = (p == null) ? null : p.getPrevSpell(); }
                for (int i = 0; i < left; i++) {
                    if (p == null) { ok = false; seenOutOfBound = true; break; }
                    if (!(p instanceof ValueLiteralSpell v)) { ok = false; seenNonLiteral = true; break; }
                    SpellValueType need = types.get(i);
                    if (!v.VALUE_TYPE.equals(need) && !need.equals(SpellValueType.ANY)) {
                        if (firstMismatchV == null) { firstMismatchV = v; firstNeededType = need; }
                        ok = false; break;
                    }
                    attemptParams.add(v.VALUE);
                    p = p.getNextSpell();
                }
            }

            // 再从右侧收集参数（从近到远）
            if (ok && right > 0) {
                SpellItemLogic n = currentSpell.getNextSpell();
                for (int j = 0; j < right; j++) {
                    if (n == null) { ok = false; seenOutOfBound = true; break; }
                    if (!(n instanceof ValueLiteralSpell v)) { ok = false; seenNonLiteral = true; break; }
                    SpellValueType need = types.get(left + j);
                    if (!v.VALUE_TYPE.equals(need) && !need.equals(SpellValueType.ANY)) {
                        if (firstMismatchV == null) { firstMismatchV = v; firstNeededType = need; }
                        ok = false; break;
                    }
                    attemptParams.add(v.VALUE);
                    n = n.getNextSpell();
                }
            }

            if (ok) {
                matchedParams = attemptParams;
                matched = true;
                break;
            }
        }

        if (!matched) {
            return paramNoOverload(caster, spellData, sequence, currentSpell);
        }

        // 使用匹配到的参数执行
        spellParams = matchedParams;

        // 如果来自实体执行阶段（SpellEntity 已写入到 spellData），则触发插件回调
        SpellEntity spellEntityRef = spellData.getCustomData("spell_entity", SpellEntity.class);
        java.util.List<BasePlugin> plugins = java.util.List.of();
        boolean enablePlugin = (spellEntityRef != null) && isExecutable(currentSpell);
        if (enablePlugin) {
            plugins = getInstalledPlugins(spellEntityRef);
            for (BasePlugin p : plugins) { p.beforeSpellExecution(spellEntityRef, currentSpell, spellData, sequence, modifiers, spellParams); }
        }

        // 检查魔力是否足够（任一系不足即判定不足）
        if (currentSpell instanceof BaseBaseSpellLogic) {
            ModUtils.Mana need = ((BaseBaseSpellLogic) currentSpell).calculateBaseMana(spellData, sequence, modifiers, spellParams);
            if (need.anyGreaterThan(myMana)) return notEnoughMana(caster, sequence, currentSpell, need, myMana, spellParams);
        }

        Map<String, Object> result = currentSpell.run(caster, spellData, sequence, modifiers, spellParams);

        ModUtils.Mana mana = new ModUtils.Mana();
        if (currentSpell instanceof BaseBaseSpellLogic) {
            mana = ((BaseBaseSpellLogic) currentSpell).calculateBaseMana(spellData, sequence, modifiers, spellParams);
        } else {
            mana = new ModUtils.Mana();
        }

        int delayTicks = 0;
        if (result.containsKey("delay")) {
            try { delayTicks = (int) result.get("delay"); } catch (Exception ignored) { delayTicks = 0; }
        }
        boolean successful = false;
        try { successful = Boolean.TRUE.equals(result.get("successful")); } catch (Exception ignored) { successful = false; }
        boolean shouldDiscard = false;
        try { shouldDiscard = Boolean.TRUE.equals(result.get("should_discard")); } catch (Exception ignored) { shouldDiscard = false; }

        StepResult step = new StepResult(shouldDiscard, successful, delayTicks, result, mana);

        if (enablePlugin) {
            for (BasePlugin p : plugins) { p.afterSpellExecution(step, spellEntityRef, currentSpell, spellData, sequence, modifiers, spellParams); }
        }

        return step;
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

    public static int indexOf(SpellSequence seq, SpellItemLogic target) {
        int i = 0;
        for (SpellItemLogic it = seq.getFirstSpell(); it != null; it = it.getNextSpell()) {
            if (it == target) return i;
            i++;
        }
        return -1;
    }

    public static int displayIndexOf(SpellSequence seq, SpellItemLogic target) {
        return indexOf(seq, target) - 1;
    }

    private static String formatActualParams(SpellSequence seq, SpellItemLogic current) {
        List<List<SpellValueType>> overloads = current.getNeededParamsType();
        int maxParams = 0;
        if (overloads != null) {
            for (List<SpellValueType> types : overloads) {
                if (types == null) continue;
                if (Objects.equals(types, List.of(SpellValueType.EMPTY))) continue;
                maxParams = Math.max(maxParams, types.size());
            }
        }
        if (maxParams <= 0) return "[]";

        int right = Math.max(0, Math.min(current.RightParamsOffset, maxParams));
        int left = maxParams - right;

        ArrayList<String> got = new ArrayList<>();

        SpellItemLogic p = current;
        for (int i = 0; i < left; i++) p = (p == null) ? null : p.getPrevSpell();
        for (int i = 0; i < left; i++) {
            if (!(p instanceof ValueLiteralSpell v)) break;
            got.add(v.VALUE_TYPE.name().toLowerCase(java.util.Locale.ROOT));
            p = p.getNextSpell();
        }

        SpellItemLogic n = current.getNextSpell();
        for (int j = 0; j < right; j++) {
            if (!(n instanceof ValueLiteralSpell v)) break;
            got.add(v.VALUE_TYPE.name().toLowerCase(java.util.Locale.ROOT));
            n = n.getNextSpell();
        }

        return "[" + String.join(", ", got) + "]";
    }

    private static String formatActualValues(List<Object> spellParams) {
        if (spellParams == null || spellParams.isEmpty()) return "[]";
        ArrayList<String> list = new ArrayList<>();
        for (Object o : spellParams) {
            if (o == null) { list.add("null"); continue; }
            SpellValueType type = SpellValueType.ANY;
            for (SpellValueType t : SpellValueType.values()) {
                if (t.matches(o)) { type = t; break; }
            }
            list.add(type.id());
        }
        return "[" + String.join(", ", list) + "]";
    }

    public static boolean setSpellError(Player caster, SpellData spellData, Component message) {
        if (spellData.hasCustomData("spell_error")) return false;
        spellData.setCustomData("spell_error", true);
        sendErrorMessageToPlayer(message, caster);
        return true;
    }

    private static StepResult paramNoOverload(Player caster, SpellData spellData, SpellSequence seq, SpellItemLogic current) {
        int index = displayIndexOf(seq, current);
        String actual = formatActualParams(seq, current);
        LOGGER.error("参数错误: spell[{}]:{} no_overload actual_params:{}", index, current.getRegistryName(), actual);
        setSpellError(caster, spellData, formatSpellError(
                Component.translatable("message.programmable_magic.error.kind.param"),
                Component.translatable("message.programmable_magic.error.detail.no_overload", index, current.getRegistryName(), actual)
        ));
        return new StepResult(true, false, 0, Map.of(), new ModUtils.Mana());
    }

    private static StepResult notEnoughMana(Player caster,
                                           SpellSequence seq,
                                           SpellItemLogic current,
                                           ModUtils.Mana need,
                                           ModUtils.Mana have,
                                           List<Object> spellParams) {
        int index = displayIndexOf(seq, current);
        String actual = formatActualValues(spellParams);
        LOGGER.error("内部错误: spell[{}]:{} not_enough_mana need:{} have:{} params:{}", index, current.getRegistryName(), need.toMap(), have.toMap(), actual);
        sendErrorMessageToPlayer(formatSpellError(
                Component.translatable("message.programmable_magic.error.kind.internal"),
                Component.translatable("message.programmable_magic.error.detail.not_enough_mana_at", index, current.getRegistryName(), actual)
        ), caster);
        return new StepResult(true, false, 0, Map.of(), new ModUtils.Mana());
    }


    public static SpellSequence calculateSpellSequence(Player player, SpellData spellData, SpellSequence seq) {
        LOGGER.debug("开始计算法术序列");
        if (spellData != null && spellData.hasCustomData("spell_error")) return seq;

        // 先寻找括号, 对每个括号内的部分进行递归计算
        List<List<SpellItemLogic>> pairs = getParenPairs(player, spellData, seq);
        if (spellData != null && spellData.hasCustomData("spell_error")) return seq;
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
        final List<SpellItemLogic> ORDER = List.of(
                new MathOperationsSpell.PowerSpell(),
                new MathOperationsSpell.MultiplicationSpell(),
                new MathOperationsSpell.DivisionSpell(),
                new MathOperationsSpell.AdditionSpell(),
                new MathOperationsSpell.SubtractionSpell(),
                new LogicalOperationsSpell.AndSpell(),
                new LogicalOperationsSpell.OrSpell()
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
                    if (L instanceof ValueLiteralSpell && R instanceof ValueLiteralSpell && operator.getNeededParamsType().contains(List.of(((ValueLiteralSpell) L).VALUE_TYPE, ((ValueLiteralSpell) R).VALUE_TYPE))) {
                        if (operator instanceof MathOperationsSpell.DivisionSpell) {
                            boolean div0 = false;
                            if (((ValueLiteralSpell) L).VALUE instanceof Double a && ((ValueLiteralSpell) R).VALUE instanceof Double b) div0 = (b == 0.0);
                            if (((ValueLiteralSpell) L).VALUE instanceof Vec3 && ((ValueLiteralSpell) R).VALUE instanceof Double b) div0 = (b == 0.0);
                            if (((ValueLiteralSpell) L).VALUE instanceof Double a && ((ValueLiteralSpell) R).VALUE instanceof Vec3) div0 = (a == 0.0);
                            if (div0) {
                                int index = displayIndexOf(seq, current);
                                String actual = formatActualParams(seq, current);
                                setSpellError(player, spellData, formatSpellError(
                                        Component.translatable("message.programmable_magic.error.kind.math"),
                                        Component.translatable("message.programmable_magic.error.detail.divide_by_zero", index, actual)
                                ));
                                return seq;
                            }
                        }
                        Map<String, Object> result = operator.run(player, spellData, seq, null, List.of(
                                ((ValueLiteralSpell) L).VALUE, ((ValueLiteralSpell) R).VALUE));

                        seq.replaceSection(L, R, new SpellSequence(List.of(new ValueLiteralSpell((SpellValueType) result.get("type"), result.get("value")))));
                    } else if (operator instanceof MathOperationsSpell.SubtractionSpell && !(L instanceof ValueLiteralSpell) && R instanceof ValueLiteralSpell) {
                        // TODO: 这里的特判并不好, 应该复用下面的处理方法
                        Map<String, Object> result = operator.run(player, spellData, seq, null, List.of(((ValueLiteralSpell) R).VALUE));
                        seq.replaceSection(current, R, new SpellSequence(List.of(new ValueLiteralSpell((SpellValueType) result.get("type"), result.get("value")))));
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
            if (isExecutable(spell) || spell instanceof ValueLiteralSpell || spell instanceof MathOperationsSpell) continue;

            if ((spell instanceof LogicalOperationsSpell.EqualSpell
                    || spell instanceof LogicalOperationsSpell.NotEqualSpell
                    || spell instanceof LogicalOperationsSpell.GreaterSpell
                    || spell instanceof LogicalOperationsSpell.GreaterEqualSpell
                    || spell instanceof LogicalOperationsSpell.LessSpell
                    || spell instanceof LogicalOperationsSpell.LessEqualSpell
                    || spell instanceof LogicalOperationsSpell.AndSpell
                    || spell instanceof LogicalOperationsSpell.OrSpell)
                    && (!(spell.getPrevSpell() instanceof ValueLiteralSpell) || !(spell.getNextSpell() instanceof ValueLiteralSpell))) continue;

            var step = SpellUtils.executeCurrentSpell(player, spellData, seq, spell, new ModUtils.Mana());
            if (!step.successful) continue;
            Map<String, Object> result = step.result;

            SpellItemLogic L = spell;
            SpellItemLogic R = spell;

            // 根据实际匹配到的重载，计算需要替换的区间
            List<List<SpellValueType>> overloads = spell.getNeededParamsType();
            if (!(overloads.isEmpty() || Objects.equals(overloads.get(0), List.of(SpellValueType.EMPTY)))) {
                int matchedLeft = -1;
                int matchedRight = -1;

                for (List<SpellValueType> types : overloads) {
                    if (Objects.equals(types, List.of(SpellValueType.EMPTY))) continue;

                    int total = types.size();
                    int right = Math.max(0, Math.min(spell.RightParamsOffset, total));
                    int left = total - right;

                    boolean ok = true;

                    // 校验左侧参数类型（从远到近）
                    SpellItemLogic p = spell;
                    for (int i = 0; i < left; i++) { p = (p == null) ? null : p.getPrevSpell(); }
                    for (int i = 0; i < left; i++) {
                        if (!(p instanceof ValueLiteralSpell v)) { ok = false; break; }
                        SpellValueType need = types.get(i);
                        if (!v.VALUE_TYPE.equals(need) && !need.equals(SpellValueType.ANY)) { ok = false; break; }
                        p = p.getNextSpell();
                    }

                    if (!ok) continue;

                    // 校验右侧参数类型（从近到远）
                    SpellItemLogic n = spell.getNextSpell();
                    for (int j = 0; j < right; j++) {
                        if (!(n instanceof ValueLiteralSpell v)) { ok = false; break; }
                        SpellValueType need = types.get(left + j);
                        if (!v.VALUE_TYPE.equals(need) && !need.equals(SpellValueType.ANY)) { ok = false; break; }
                        n = n.getNextSpell();
                    }

                    if (!ok) continue;

                    matchedLeft = left;
                    matchedRight = right;
                    break;
                }

                if (matchedLeft >= 0) {
                    for (int i = 0; i < matchedLeft; i++) { L = L.getPrevSpell(); }
                    for (int i = 0; i < matchedRight; i++) { R = R.getNextSpell(); }
                }
            }

            if (!result.containsKey("type")) { seq.replaceSection(L, R, new SpellSequence()); flag = true; continue; }
            seq.replaceSection(L, R, new SpellSequence(List.of(new ValueLiteralSpell((SpellValueType) result.get("type"), result.get("value")))));
            flag = true;
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

    public static boolean isExecutable(SpellItemLogic currentSpell) { return
	            !(currentSpell.getSpellType() == SpellItemLogic.SpellType.COMPUTE_MOD
	            || currentSpell instanceof LogicalOperationsSpell
	            || currentSpell instanceof BlockConditionSpell
	            || currentSpell instanceof ValueLiteralSpell); }

    public static Map<Component, List<ItemStack>> getSpellsGroupedBySubCategory(SpellItemLogic.SpellType type) {
        var map = new LinkedHashMap<Component, List<ItemStack>>();
        for (var entry : SpellRegistry.getRegisteredSpells().entrySet()) {
            var logic = entry.getValue().get();
            if (logic.getSpellType() != type) continue;
            var sub = logic.getSubCategory();
            var list = map.get(sub);
            if (list == null) { list = new ArrayList<>(); map.put(sub, list); }
            list.add(new ItemStack(entry.getKey().get()));
        }
        return map;
    }

    public static final Map<String, SpellItemLogic.SpellType> stringSpellTypeMap = Map.of(
            "compute", SpellItemLogic.SpellType.COMPUTE_MOD,
            "adjust", SpellItemLogic.SpellType.ADJUST_MOD,
            "control", SpellItemLogic.SpellType.CONTROL_MOD,
            "base", SpellItemLogic.SpellType.BASE_SPELL
    );
}
