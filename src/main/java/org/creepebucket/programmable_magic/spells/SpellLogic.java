package org.creepebucket.programmable_magic.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.compute_mod.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellUtils.getSeps;
import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public class SpellLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellLogic");
    
    private final List<ItemStack> spellStacks;
    private final Player player;
    private SpellSequence spellSequence;
    private SpellData spellData;
    
    public SpellLogic(List<ItemStack> spellStacks, Player player) {
        this.spellStacks = spellStacks;
        this.player = player;
        this.spellSequence = new SpellSequence();
        
        LOGGER.info("=== SpellLogic 构造函数开始 ===");
        LOGGER.info("玩家: {}, 法术数量: {}", player.getName().getString(), spellStacks.size());
        
        // 初始化法术数据
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 lookDirection = player.getLookAngle();
        this.spellData = new SpellData(player, playerPos, lookDirection);
        
        LOGGER.info(String.format("法术数据初始化完成 - 位置: (%.2f, %.2f, %.2f), 方向: (%.2f, %.2f, %.2f)",
                playerPos.x, playerPos.y, playerPos.z, lookDirection.x, lookDirection.y, lookDirection.z));
        LOGGER.info("=== SpellLogic 构造函数完成 ===");
    }
    
    /**
     * 执行法术逻辑的主入口
     */
    public void execute() {
        // 1. 将ItemStack转换为SpellItemLogic
        convertItemStacksToLogic();
        // 2. 转换NumberDigitSpell -> ValueLiteralSpell 并去除分隔符
        convertNumberDigitsToValues();
        // BUGFIX: 在法术序列之前加一个占位ValueLiteralSpell, 防止第一次简化序列的lastBoundarySpell.getNextSpell()未包含实际序列的第一个法术
        spellSequence.addFirst(new ValueLiteralSpell(SpellValueType.EMPTY, 0));
        // 3. 创建法术实体
        SpellEntity spellEntity = createSpellEntity();
        // 4. 生成实体到世界
        player.level().addFreshEntity(spellEntity);
    }
    
    /**
     * 步骤1
     * 将ItemStack转换为SpellItemLogic
     */
    private void convertItemStacksToLogic() {
        for (ItemStack stack : spellStacks) {
            SpellItemLogic logic = SpellRegistry.createSpellLogic(stack.getItem());
            if (logic != null) {
                spellSequence.addLast(logic);
            } else {
                LOGGER.warn("无法为物品创建法术逻辑: {}", stack);
            }
        }
    }

    /**
     * 步骤2
     * 将NumberDigitSpell转换为ValueLiteralSpell并去除分隔符
     */
    private void convertNumberDigitsToValues() {
        LOGGER.debug("开始将数字法术转换为值法术");
        List<List<SpellItemLogic>> pairsNumberDigit = getPairsNumberDigit(spellSequence); // {{L1, R1}, {L2, R2}, ...}

        for (List<SpellItemLogic> pair : pairsNumberDigit) {
            spellSequence.replaceSection(pair.get(0), pair.get(1), new SpellSequence(List.of(
                    new ValueLiteralSpell(NUMBER, pair.get(1).run(player, spellData, spellSequence, null, null).get("value")))));
        }

        // 去除分隔符
        List<SpellItemLogic> seps = getSeps(spellSequence);
        for (SpellItemLogic sep : seps) {
            spellSequence.replaceSection(sep, sep, new SpellSequence());
        }
    }

    /**
     * 步骤5
     * 创建法术实体
     */
    private SpellEntity createSpellEntity() {
        LOGGER.debug("开始创建法术实体");
        LOGGER.debug(String.format("玩家位置: (%.2f, %.2f, %.2f)",
                player.getX(), player.getY(), player.getZ()));
        LOGGER.debug(String.format("玩家朝向: (%.2f, %.2f, %.2f)",
                player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z));

        SpellEntity entity = new SpellEntity(player.level(), player, spellSequence, spellData);
        LOGGER.debug("法术实体创建成功，实体ID: {}", entity.getId());
        
        return entity;
    }

    /*
     * --------------------------------------------------
     * 在法术逻辑主函数中使用的工具函数
     */

    private List<List<SpellItemLogic>> getPairsNumberDigit(SpellSequence seq) {
        List<List<SpellItemLogic>> pairs = new ArrayList<>();
        for (SpellItemLogic it = seq.getFirstSpell(); it != null; ) {
            // 寻找一段连续的数字法术
            if (!(it instanceof NumberDigitSpell)) {
                it = it.getNextSpell();
                continue;
            }

            SpellItemLogic left = it;
            SpellItemLogic right = it;
            while (right.getNextSpell() instanceof NumberDigitSpell) { right = right.getNextSpell(); }

            pairs.add(List.of(left, right));
            it = right.getNextSpell();
        }

        return pairs;
    }

}
