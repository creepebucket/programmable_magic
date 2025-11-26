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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

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
        
        // 1.5 简化修饰-基础串中的计算表达式
            LOGGER.info("步骤 1.5: 开始简化修饰-基础串中的计算表达式");
        preprocessComputeForModifiers();
            LOGGER.info("步骤 1.5 完成: 表达式已简化，序列长度 {}", spellSequence.size());
        
        // 2. 计算总魔力消耗
            LOGGER.info("步骤 2: 开始计算总魔力消耗");
        // calculateTotalManaCost();
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
        // spellEntity.setSpellSequence(spellSequence, spellData);
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
     * 按照规则对法术序列进行预处理：
     * - 以 BASE_SPELL 为边界，将前面的修饰串拆为若干「计算-修饰串」：按非 compute 切割
     * - 对每个「计算串」，再按分隔符法术 compute_mod 切割为多个表达式
     * - 每个表达式尝试计算，若得到整数或可表示为一串数字（含一元负号），则用结果的数字序列替换原表达式；否则保留原样
     * - 分隔符 compute_mod 原样保留，用以分隔多个表达式（如向量）
     */
    private void preprocessComputeForModifiers() {
        if (spellSequence.isEmpty()) return;

        List<SpellItemLogic> original = new ArrayList<>(spellSequence);
        List<SpellItemLogic> result = new ArrayList<>();

        List<SpellItemLogic> bufferMods = new ArrayList<>();
        for (SpellItemLogic s : original) {
            if (s.getSpellType() == SpellItemLogic.SpellType.BASE_SPELL) {
                // 处理前置修饰串
                result.addAll(simplifyModifierChain(bufferMods));
                bufferMods.clear();
                // 加入基础法术
                result.add(s);
            } else {
                bufferMods.add(s);
            }
        }
        // 末尾剩余修饰串（若存在）
        result.addAll(simplifyModifierChain(bufferMods));

        spellSequence.clear();
        spellSequence.addAll(result);
    }

    private List<SpellItemLogic> simplifyModifierChain(List<SpellItemLogic> mods) {
        if (mods == null || mods.isEmpty()) return List.of();

        List<SpellItemLogic> out = new ArrayList<>();
        List<SpellItemLogic> computeBuf = new ArrayList<>();

        for (SpellItemLogic s : mods) {
            if (s.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) {
                // 先把前面的计算部分按分隔符切割并简化后输出
                out.addAll(simplifyComputeListByDelimiter(computeBuf));
                computeBuf.clear();
                // 再输出当前修饰法术
                out.add(s);
            } else {
                computeBuf.add(s);
            }
        }
        // 结尾残留的计算部分
        out.addAll(simplifyComputeListByDelimiter(computeBuf));

        return out;
    }

    private List<SpellItemLogic> simplifyComputeListByDelimiter(List<SpellItemLogic> computeList) {
        if (computeList == null || computeList.isEmpty()) return List.of();

        List<SpellItemLogic> out = new ArrayList<>();
        List<SpellItemLogic> expr = new ArrayList<>();

        for (int i = 0; i < computeList.size(); i++) {
            SpellItemLogic s = computeList.get(i);
            if (isDelimiter(s)) {
                // 处理前一个表达式
                out.addAll(replaceExpressionOrKeep(expr));
                expr.clear();
                // 保留分隔符
                out.add(s);
            } else if (isExpressionToken(s)) {
                // 仍属于表达式的一部分
                expr.add(s);
            } else {
                // 非表达式的 compute_mod（如：构建XYZ向量等）视为边界：先结清前面的表达式，再原样追加该法术
                out.addAll(replaceExpressionOrKeep(expr));
                expr.clear();
                out.add(s);
            }
        }
        // 末尾表达式
        out.addAll(replaceExpressionOrKeep(expr));

        return out;
    }

    private List<SpellItemLogic> replaceExpressionOrKeep(List<SpellItemLogic> expr) {
        if (expr == null || expr.isEmpty()) return List.of();
        Double v = evaluateExpression(expr);
        if (v == null || v.isNaN() || v.isInfinite()) {
            return new ArrayList<>(expr);
        }
        // 用数字字面量节点替换表达式，支持小数
        List<SpellItemLogic> one = new ArrayList<>();
        return one;
    }

    private boolean isDelimiter(SpellItemLogic s) {
        return s != null && s.getSpellType() == SpellItemLogic.SpellType.COMPUTE_MOD
                && Objects.equals(s.getRegistryName(), "compute_mod");
    }

    // 不再需要将表达式展开为 digit token 序列

    // ===== 计算表达式（从 SimpleCompute/NumberComputeBase 的逻辑抽取的等价实现） =====
    private Double evaluateExpression(List<SpellItemLogic> expr) {
        if (expr == null || expr.isEmpty()) return null;

        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < expr.size(); ) {
            SpellItemLogic s = expr.get(i);
            String rn = s.getRegistryName();
            if (isDigitToken(rn)) {
                StringBuilder digits = new StringBuilder();
                while (i < expr.size() && isDigitToken(expr.get(i).getRegistryName())) {
                    digits.append(expr.get(i).getRegistryName().charAt(expr.get(i).getRegistryName().length() - 1));
                    i++;
                }
                tokens.add(digits.toString());
            } else {
                String op = mapOperator(rn);
                if (op != null) {
                    tokens.add(op);
                } else if (isExpressionToken(s)) {
                    // 理论上不会到这里（已被 isDigitToken/mapOperator 捕捉），兜底忽略
                } else {
                    // 遇到非表达式的 compute_mod 或其他类型，视为表达式边界，中止解析
                    break;
                }
                i++;
            }
        }

        tokens = normalizeUnaryMinus(tokens);

        // 仅当表达式“自包含”时才尝试求值（避免跨段括号导致的未闭合报错）
        if (!looksSelfContained(tokens)) return null;

        if (tokens.isEmpty()) return null;
        return evaluateTokens(tokens);
    }

    private boolean isDigitToken(String rn) {
        if (rn == null) return false;
        if (!rn.startsWith("compute_")) return false;
        char c = rn.charAt(rn.length() - 1);
        return c >= '0' && c <= '9';
    }

    private String mapOperator(String rn) {
        if (rn == null) return null;
        return switch (rn) {
            case "compute_add" -> "+";
            case "compute_sub" -> "-";
            case "compute_mul" -> "*";
            case "compute_div" -> "/";
            case "compute_pow" -> "^";
            case "compute_lparen" -> "(";
            case "compute_rparen" -> ")";
            default -> null;
        };
    }

    private boolean isExpressionToken(SpellItemLogic s) {
        if (s == null) return false;
        if (s.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) return false;
        String rn = s.getRegistryName();
        // 括号在预处理阶段视为边界，不参与数值化表达式
        if ("compute_lparen".equals(rn) || "compute_rparen".equals(rn)) return false;
        return isDigitToken(rn) || mapOperator(rn) != null;
    }

    private List<String> normalizeUnaryMinus(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return tokens;
        List<String> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if ("-".equals(t)) {
                String prev = out.isEmpty() ? null : out.get(out.size() - 1);
                boolean unary = (out.isEmpty() || "(".equals(prev) || isBinaryOp(prev));
                if (unary && i + 1 < tokens.size()) {
                    String next = tokens.get(i + 1);
                    if (next.matches("\\d+")) {
                        out.add("-" + next);
                        i++;
                        continue;
                    }
                }
            }
            out.add(t);
        }
        return out;
    }

    private boolean isBinaryOp(String s) {
        return s != null && (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("^") );
    }

    private int precedence(String op) {
        return switch (op) {
            case "^" -> 4;
            case "*", "/" -> 3;
            case "+", "-" -> 2;
            default -> 0;
        };
    }

    private boolean isRightAssoc(String op) { return "^".equals(op); }

    private Double evaluateTokens(List<String> tokens) {
        Deque<Double> values = new ArrayDeque<>();
        Deque<String> ops = new ArrayDeque<>();
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (t.isEmpty()) continue;
            if ("(".equals(t)) {
                ops.push(t);
            } else if (")".equals(t)) {
                boolean matched = false;
                while (!ops.isEmpty()) {
                    if ("(".equals(ops.peek())) { ops.pop(); matched = true; break; }
                    if (!applyTop(values, ops.pop())) return Double.NaN;
                }
                if (!matched) return Double.NaN;
            } else if (isBinaryOp(t)) {
                while (!ops.isEmpty() && !"(".equals(ops.peek()) &&
                        (isRightAssoc(t) ? precedence(t) < precedence(ops.peek()) : precedence(t) <= precedence(ops.peek()))) {
                    if (!applyTop(values, ops.pop())) return Double.NaN;
                }
                ops.push(t);
            } else {
                try {
                    values.push(Double.parseDouble(t));
                } catch (NumberFormatException e) {
                    return Double.NaN;
                }
            }
        }
        while (!ops.isEmpty()) {
            if ("(".equals(ops.peek())) return Double.NaN;
            if (!applyTop(values, ops.pop())) return Double.NaN;
        }
        if (values.isEmpty()) return Double.NaN;
        return values.pop();
    }

    private boolean looksSelfContained(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return false;
        String last = tokens.get(tokens.size() - 1);
        if (!(last.matches("-?\\d+") || ")".equals(last))) return false;
        Deque<Character> st = new ArrayDeque<>();
        for (String t : tokens) {
            if ("(".equals(t)) st.push('(');
            else if (")".equals(t)) { if (st.isEmpty()) return false; st.pop(); }
        }
        return st.isEmpty();
    }

    private boolean applyTop(Deque<Double> values, String op) {
        if (values.size() < 2) return false;
        double b = values.pop();
        double a = values.pop();
        double r;
        switch (op) {
            case "+" -> r = a + b;
            case "-" -> r = a - b;
            case "*" -> r = a * b;
            case "/" -> { if (b == 0.0) return false; r = a / b; }
            case "^" -> r = Math.pow(a, b);
            default -> { return false; }
        }
        values.push(r);
        return true;
    }

    // 通过 registryName 创建对应的 SpellItemLogic 实例（从注册表的 supplier 映射）
    private static Map<String, Supplier<SpellItemLogic>> NAME_TO_SUPPLIER;
    private SpellItemLogic newLogicByRegistryName(String name) {
        if (name == null) return null;
        if (NAME_TO_SUPPLIER == null) {
            NAME_TO_SUPPLIER = new HashMap<>();
            for (Supplier<net.minecraft.world.item.Item> itemSup : SpellRegistry.getRegisteredSpells().keySet()) {
                Supplier<SpellItemLogic> logicSup = SpellRegistry.getRegisteredSpells().get(itemSup);
                try {
                    SpellItemLogic inst = logicSup.get();
                    if (inst != null) {
                        NAME_TO_SUPPLIER.put(inst.getRegistryName(), logicSup);
                    }
                } catch (Exception ignored) {}
            }
        }
        Supplier<SpellItemLogic> sup = NAME_TO_SUPPLIER.get(name);
        return sup != null ? sup.get() : null;
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
        
        SpellEntity entity = new SpellEntity(player.level(), player, spellSequence);
        LOGGER.debug("法术实体创建成功，实体ID: {}", entity.getId());
        
        return entity;
    }
}
