package org.creepebucket.programmable_magic.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.adjust_mod.BaseAdjustModLogic;
import org.creepebucket.programmable_magic.spells.base_spell.BaseBaseSpellLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.ValueLiteralSpell;
import org.creepebucket.programmable_magic.spells.control_mod.BaseControlModLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
}
