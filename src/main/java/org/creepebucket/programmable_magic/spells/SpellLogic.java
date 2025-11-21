package org.creepebucket.programmable_magic.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.items.mana_cell.BaseManaCell;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellLogic");
    
    private final List<ItemStack> spellStacks;
    private final Player player;
    private final List<SpellItemLogic> spellSequence;
    private SpellData spellData;
    
    public SpellLogic(List<ItemStack> spellStacks, Player player) {
        this.spellStacks = spellStacks;
        this.player = player;
        this.spellSequence = new ArrayList<>();
        
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
        LOGGER.info("=== 开始执行法术逻辑 ===");
        
        try {
        // 1. 将ItemStack转换为SpellItemLogic
            LOGGER.info("步骤 1: 开始转换 ItemStack 到 SpellItemLogic");
        convertItemStacksToLogic();
            LOGGER.info("步骤 1 完成: 成功转换 {} 个法术逻辑", spellSequence.size());
        
        // 2. 计算总魔力消耗
            LOGGER.info("步骤 2: 开始计算总魔力消耗");
        calculateTotalManaCost();
            LOGGER.info("步骤 2 完成: 魔力消耗计算完成");
        
        // 3. 检查魔力是否足够
            LOGGER.info("步骤 3: 检查魔力是否足够");
        if (!checkManaAvailability()) {
                LOGGER.warn("魔力不足，法术执行终止");
            return;
        }
            LOGGER.info("步骤 3 完成: 魔力检查通过");
        
        // 4. 消耗魔力
            LOGGER.info("步骤 4: 开始消耗魔力");
        consumeMana();
            LOGGER.info("步骤 4 完成: 魔力消耗完成");
        
        // 5. 创建法术实体
            LOGGER.info("步骤 5: 开始创建法术实体");
        SpellEntity spellEntity = createSpellEntity();
            LOGGER.info("步骤 5 完成: 法术实体创建成功");
        
        // 6. 将法术序列传递给实体
            LOGGER.info("步骤 6: 将法术序列传递给实体");
        spellEntity.setSpellSequence(spellSequence, spellData);
            LOGGER.info("步骤 6 完成: 法术序列已设置到实体");
        
        // 7. 生成实体到世界
            LOGGER.info("步骤 7: 将法术实体添加到世界");
        player.level().addFreshEntity(spellEntity);
            LOGGER.info("步骤 7 完成: 法术实体已添加到世界");
            
            LOGGER.info("=== 法术逻辑执行完成 ===");
            
        } catch (Exception e) {
            LOGGER.error("执行法术逻辑时发生错误", e);
        }
    }
    
    /**
     * 将ItemStack转换为SpellItemLogic
     */
    private void convertItemStacksToLogic() {
        LOGGER.debug("开始转换 {} 个 ItemStack 到 SpellItemLogic", spellStacks.size());
        
        for (int i = 0; i < spellStacks.size(); i++) {
            ItemStack stack = spellStacks.get(i);
            LOGGER.debug("转换法术 {}: {} x{}", i + 1, stack.getDisplayName().getString(), stack.getCount());
            
            SpellItemLogic logic = SpellRegistry.createSpellLogic(stack.getItem());
            if (logic != null) {
                spellSequence.add(logic);
                LOGGER.debug("法术 {} 转换成功，类型: {}", i + 1, logic.getClass().getSimpleName());
            } else {
                LOGGER.warn("法术 {} 转换失败，无法创建 SpellItemLogic", i + 1);
            }
        }
        
        LOGGER.info("ItemStack 转换完成，成功转换 {} 个，失败 {} 个", 
            spellSequence.size(), spellStacks.size() - spellSequence.size());
    }
    
    /**
     * 计算总魔力消耗（按组计算：MMMM...MMB 为一组，组内右->左应用）
     */
    private void calculateTotalManaCost() {
        LOGGER.debug("开始计算总魔力消耗，法术序列大小: {}", spellSequence.size());

        Map<String, Double> totals = SpellCostCalculator.computeRequiredManaFromLogics(spellSequence, player);

        // 将 totals 写回 spellData
        for (String manaType : List.of("radiation", "temperature", "momentum", "pressure")) {
            spellData.setManaCost(manaType, totals.getOrDefault(manaType, 0.0));
        }

        LOGGER.info("魔力消耗计算完成:");
        for (String manaType : List.of("radiation", "temperature", "momentum", "pressure")) {
            double cost = spellData.getManaCost(manaType);
            if (cost > 0) {
                LOGGER.info("  {}: {}", manaType, String.format("%.2f", cost));
            }
        }
    }
    
    /**
     * 检查魔力是否足够
     */
    private boolean checkManaAvailability() {
        LOGGER.debug("开始检查魔力是否足够");
        
        // 检查玩家背包中的魔力单元
        int totalManaCells = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BaseManaCell) {
                totalManaCells++;
            }
        }
        
        LOGGER.debug("玩家背包中找到 {} 个魔力单元", totalManaCells);
        
        // 检查四种魔力类型是否足够
        List<String> insufficientTypes = new ArrayList<>();
        for (String manaType : List.of("radiation", "temperature", "momentum", "pressure")) {
            double required = spellData.getManaCost(manaType);
            if (required > 0) {
                LOGGER.debug("检查 {} 魔力类型，需要: {}", manaType, String.format("%.2f", required));
                
                double totalAvailable = 0.0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() instanceof BaseManaCell manaCell) {
                        totalAvailable += manaCell.getMana(stack, manaType);
                    }
                }
                
                LOGGER.debug("{} 魔力类型可用: {}, 需要: {}", manaType, String.format("%.2f", totalAvailable), String.format("%.2f", required));
                
                if (totalAvailable < required) {
                    LOGGER.warn("{} 魔力类型不足: 需要 {}，可用 {}", manaType, String.format("%.2f", required), String.format("%.2f", totalAvailable));
                    insufficientTypes.add(String.format("%s: 需要%s, 可用%s",
                        getManaTypeDisplayName(manaType),
                        org.creepebucket.programmable_magic.ModUtils.FormattedManaString(required),
                        org.creepebucket.programmable_magic.ModUtils.FormattedManaString(totalAvailable)));
                }
            }
        }
        
        // 如果有魔力不足，发送详细消息给玩家
        if (!insufficientTypes.isEmpty()) {
            Component message = Component.literal("§c魔力不足！缺少以下魔力类型：");
            player.displayClientMessage(message, false);
            
            for (String insufficientType : insufficientTypes) {
                Component detailMessage = Component.literal("§7- " + insufficientType);
                player.displayClientMessage(detailMessage, false);
            }
            
            return false;
        }
        
        LOGGER.info("魔力检查通过，所有类型都足够");
        return true;
    }
    
    /**
     * 获取魔力类型的显示名称
     */
    private String getManaTypeDisplayName(String manaType) {
        return switch (manaType) {
            case "radiation" -> "辐射";
            case "temperature" -> "温度";
            case "momentum" -> "动量";
            case "pressure" -> "压力";
            default -> manaType;
        };
    }
    
    /**
     * 消耗魔力
     */
    private void consumeMana() {
        LOGGER.debug("开始消耗魔力");
        
        for (String manaType : List.of("radiation", "temperature", "momentum", "pressure")) {
            double required = spellData.getManaCost(manaType);
            if (required > 0) {
                LOGGER.debug("消耗 {} 魔力类型: {}", manaType, String.format("%.2f", required));
                consumeManaType(manaType, required);
            }
        }
        
        LOGGER.info("魔力消耗完成");
    }
    
    /**
     * 消耗特定类型的魔力
     */
    private void consumeManaType(String manaType, double amount) {
        LOGGER.debug("开始消耗 {} 魔力类型，总量: {}", manaType, String.format("%.2f", amount));
        
        double remaining = amount;
        List<ItemStack> modifiedStacks = new ArrayList<>();
        
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BaseManaCell manaCell) {
                double available = manaCell.getMana(stack, manaType);
                double toConsume = Math.min(available, remaining);
                
                if (toConsume > 0) {
                    LOGGER.debug("从槽位 {} 消耗 {} 魔力: {} -> {}", i, manaType, String.format("%.2f", available), String.format("%.2f", (available - toConsume)));
                    boolean success = manaCell.addMana(stack, manaType, -toConsume);
                    if (success) {
                        remaining -= toConsume;
                        modifiedStacks.add(stack);
                        // 强制标记物品栈已修改，确保同步到客户端
                        player.getInventory().setChanged();
                    } else {
                        LOGGER.warn("魔力扣除失败: 槽位 {}, 类型 {}, 尝试扣除 {}", i, manaType, String.format("%.2f", toConsume));
                    }
                }
            }
        }
        
        // 额外的同步确保
        if (!modifiedStacks.isEmpty()) {
            LOGGER.debug("强制同步 {} 个修改的物品栈到客户端", modifiedStacks.size());
            // 通知客户端背包已更改
            player.containerMenu.broadcastChanges();
        }
        
        if (remaining > 0.0) {
            LOGGER.warn("{} 魔力类型消耗不完整，剩余: {}", manaType, String.format("%.2f", remaining));
        } else {
            LOGGER.debug("{} 魔力类型消耗完成", manaType);
        }
    }
    
    /**
     * 创建法术实体
     */
    private SpellEntity createSpellEntity() {
        LOGGER.debug("开始创建法术实体");
        LOGGER.debug(String.format("玩家位置: (%.2f, %.2f, %.2f)",
                player.getX(), player.getY(), player.getZ()));
        LOGGER.debug(String.format("玩家朝向: (%.2f, %.2f, %.2f)",
                player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z));
        
        SpellEntity entity = new SpellEntity(player.level(), player);
        LOGGER.debug("法术实体创建成功，实体ID: {}", entity.getId());
        
        return entity;
    }
}
