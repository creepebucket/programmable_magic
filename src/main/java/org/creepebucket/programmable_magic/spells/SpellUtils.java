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

        public StepResult(boolean shouldDiscard, boolean successful, int delayTicks) {
            this.shouldDiscard = shouldDiscard;
            this.successful = successful;
            this.delayTicks = delayTicks;
        }
    }

    /**
     * 执行当前索引的法术一步，并返回执行结果。
     * 注意：保持与原 SpellEntity.tick() 一致的流程与行为（包含报错时丢弃实体的信号）。
     */
    public static StepResult executeCurrentSpell(Player caster,
                                                SpellData spellData,
                                                List<SpellItemLogic> spellSequence,
                                                int currentSpellIndex) {
        if (currentSpellIndex < 0 || currentSpellIndex >= spellSequence.size()) {
            return new StepResult(true, false, 0);
        }

        SpellItemLogic currentSpell = spellSequence.get(currentSpellIndex);

        // 收集法术参数和修饰法术
        List<SpellItemLogic> modifiers = new ArrayList<>();
        List<Object> spellParams = new ArrayList<>();

        if (currentSpell instanceof BaseBaseSpellLogic) {
            // 向左寻找修饰, 直到遇到上一个基础法术
            for (int i = currentSpellIndex - 1; i >= 0; i--) {
                if (i < 0) break;
                SpellItemLogic spell = spellSequence.get(i);
                if (spell instanceof BaseBaseSpellLogic) break;
                if (!(spell instanceof BaseAdjustModLogic || spell instanceof BaseControlModLogic)) {
                    continue;
                }
                modifiers.add(spell);
            }
        }

        List<Object> neededParamsType = currentSpell.getNeededParamsType();

        // 收集法术参数然后检查类型（保留原实现的边界与类型检查逻辑）
        for (int i = currentSpellIndex - spellSequence.size() + currentSpell.RightParamsOffset;
             i < currentSpellIndex + currentSpell.RightParamsOffset; i++) {
            int paramIndex = i - currentSpellIndex + currentSpell.RightParamsOffset;

            if (i < 0 || i >= spellSequence.size()) {
                LOGGER.error("[ProgrammableMagic:SpellEntity] 在搜索参数时突破边界: 当前法术: {}", currentSpell.getRegistryName());
                sendErrorMessageToPlayer(Component.translatable("message.programmable_magic.error.wand.param_search_out_of_bound"), caster);
                return new StepResult(true, false, 0);
            }
            if (i == currentSpellIndex) continue;

            SpellItemLogic paramSpell = spellSequence.get(i);
            if (!(paramSpell instanceof ValueLiteralSpell)) {
                LOGGER.error("[ProgrammableMagic:SpellEntity] 尝试收集参数时发现参数不是ValueLiteralSpell类型: 当前法术: {}", currentSpell.getRegistryName());
                sendErrorMessageToPlayer(Component.translatable("message.programmable_magic.error.wand.internal_bug"), caster);
                return new StepResult(true, false, 0);
            }

            ValueLiteralSpell param = (ValueLiteralSpell) paramSpell;
            Object neededType = neededParamsType.get(paramIndex);
            // 按原逻辑：不等或等于 ANY 时都视为错误（尽管看起来更合理的是允许 ANY）
            if (!param.VALUE_TYPE.equals(neededType) || neededType.equals(SpellValueType.ANY)) {
                LOGGER.error("[ProgrammableMagic:SpellEntity] 尝试收集参数时发现参数类型错误: 当前法术: {} 参数类型: {} 需要的类型: {}",
                        currentSpell.getRegistryName(), param.VALUE_TYPE, neededParamsType.get(paramIndex));
                sendErrorMessageToPlayer(Component.translatable(
                        "message.programmable_magic.error.wand.param_type_error",
                        currentSpell.getRegistryName(), param.VALUE_TYPE, neededParamsType.get(paramIndex)
                ), caster);
                return new StepResult(true, false, 0);
            }

            spellParams.add(param.VALUE);
        }

        Map<String, Object> result = currentSpell.run(caster, spellData, spellSequence, currentSpellIndex, modifiers, spellParams);

        int delayTicks = 0;
        if (result.containsKey("delay")) {
            try { delayTicks = (int) result.get("delay"); } catch (Exception ignored) { delayTicks = 0; }
        }
        boolean successful = false;
        try { successful = Boolean.TRUE.equals(result.get("successful")); } catch (Exception ignored) { successful = false; }

        return new StepResult(false, successful, delayTicks);
    }
}

