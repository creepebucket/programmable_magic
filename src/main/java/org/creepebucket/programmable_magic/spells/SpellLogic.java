package org.creepebucket.programmable_magic.spells;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.compute_mod.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.ModUtils.sendErrorMessageToPlayer;
import static org.creepebucket.programmable_magic.spells.SpellUtils.getSeps;
import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public class SpellLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellLogic");
    
    private final List<ItemStack> spellStacks;
    private final Player player;
    private SpellSequence spellSequence;
    private SpellData spellData;
    private List<ItemStack> pending; // 待从背包扣除的真实物品（由占位符绑定）
    private double chargeSeconds = 0.0; // 按压时长（秒）
    
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
        this.pending = new ArrayList<>();
        
        LOGGER.info(String.format("法术数据初始化完成 - 位置: (%.2f, %.2f, %.2f), 方向: (%.2f, %.2f, %.2f)",
                playerPos.x, playerPos.y, playerPos.z, lookDirection.x, lookDirection.y, lookDirection.z));
        LOGGER.info("=== SpellLogic 构造函数完成 ===");
    }

    public SpellLogic(List<ItemStack> spellStacks, Player player, double chargeSeconds) {
        this(spellStacks, player);
        this.chargeSeconds = Math.max(0.0, chargeSeconds);
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
        // 3. 在背包中删除 WandItemPlaceholder 绑定的物品
        boolean r = consumePendingItemsFromInventory();
        if (!r) {
            // 未能按预期扣除物品
            sendErrorMessageToPlayer(Component.translatable("message.programmable_magic.error.wand.placeholder_consume_failed"), player);
            return;
        }
        // 4. 创建法术实体
        SpellEntity spellEntity = createSpellEntity();
        // 5. 生成实体到世界
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
            } else if (stack.getItem() instanceof WandItemPlaceholder){
                // 源头已默认绑定 AIR；直接读取并解析绑定物品
                String key = stack.get(ModDataComponents.WAND_PLACEHOLDER_ITEM_ID.get());
                ResourceLocation rl = ResourceLocation.tryParse(key);
                ItemStack real = new ItemStack(BuiltInRegistries.ITEM.get(rl).get());
                spellSequence.addLast(new ValueLiteralSpell(SpellValueType.ITEM, real));
                if (!real.isEmpty()) pending.add(real.copy());
            } else {
                // 普通物品直接作为字面量值
                spellSequence.addLast(new ValueLiteralSpell(SpellValueType.ITEM, stack));
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
     * 从玩家背包中扣除由占位符绑定的真实物品（每个占位符扣 1 个）。
     */
    private boolean consumePendingItemsFromInventory() {
        boolean flag = false;
        boolean success = true;
        var inv = player.getInventory();
        for (ItemStack need : pending) {
            Item targetItem = need.getItem();
            int size = inv.getContainerSize();
            flag = false;
            for (int i = 0; i < size; i++) {
                ItemStack cur = inv.getItem(i);
                if (cur.isEmpty()) continue;
                if (cur.is(targetItem)) {
                    cur.shrink(1);
                    if (cur.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
                    flag = true;
                }
            }
            if (!flag) success = false;
        }
        return success;
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

        // 依据充能秒数与魔杖功率（W）换算魔力：W * s = J；1 mana = 1 kJ
        double manaMult = 1.0;
        double chargeRateW = 0.0;
        net.minecraft.world.item.ItemStack heldMain = player.getMainHandItem();
        net.minecraft.world.item.ItemStack heldOff = player.getOffhandItem();
        if (heldMain.getItem() instanceof org.creepebucket.programmable_magic.items.wand.BaseWand w) {
            manaMult = w.getManaMult();
            chargeRateW = w.getChargeRate();
        } else if (heldOff.getItem() instanceof org.creepebucket.programmable_magic.items.wand.BaseWand w2) {
            manaMult = w2.getManaMult();
            chargeRateW = w2.getChargeRate();
        }
        double energyJ = chargeRateW * this.chargeSeconds; // 焦耳
        double chargedMana = energyJ / 1000.0; // 转换为 mana（1 mana = 1 kJ）
        double supply = (manaMult == 0.0) ? chargedMana : chargedMana / manaMult; // 发射时除以魔杖倍率
        Mana initialMana = new Mana(supply, supply, supply, supply);

        SpellEntity entity = new SpellEntity(player.level(), player, spellSequence, spellData, initialMana);
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
