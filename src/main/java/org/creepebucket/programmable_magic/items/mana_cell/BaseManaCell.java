package org.creepebucket.programmable_magic.items.mana_cell;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseManaCell extends Item {

    //魔力单元的属性
    private final double MAX_MANA_AMOUNT;
    private final int MAX_MANA_TYPE;

    public BaseManaCell(Properties properties, double maxManaAmount, int maxManaType) {super(properties);
        MAX_MANA_AMOUNT = maxManaAmount;
        MAX_MANA_TYPE = maxManaType;
    }


    //魔力getter/setter
    public Map<String, Double> getManaMap(ItemStack itemStack) {
        Map<String, Double> properties = itemStack.get(ModDataComponents.MANA.get());
        return properties != null ? properties : new HashMap<>();
    }

    public double getMana(ItemStack itemStack, String key) {
        Map<String, Double> mana = getManaMap(itemStack);
        return mana.getOrDefault(key, 0.0);
    }

    public void setMana(ItemStack itemStack, String key, double value) {
        // 复制为可变映射，避免对不可变实现进行修改
        Map<String, Double> mana = new HashMap<>(getManaMap(itemStack));
        mana.put(key, value);
        itemStack.set(ModDataComponents.MANA.get(), mana);
    }

    /*
    往单元添加魔力 value负数就减少
    返回false操作不成功(撞上下限了), 操作被取消
     */
    public boolean addMana(ItemStack itemStack, String key, double value) {
        Map<String, Double> manaMap = getManaMap(itemStack);
        double current = getMana(itemStack, key);

        // 类型数量仅在正向新增一种新类型时检查
        if (value > 0) {
            int usedTypes = 0;
            for (double v : manaMap.values()) {
                if (v > 0.0) usedTypes++;
            }
            boolean isNewType = current == 0.0 && value > 0.0;
            if (isNewType && usedTypes >= MAX_MANA_TYPE) {
                return false;
            }
        }

        // 上下限检查
        double next = current + value;
        if (0.0 <= next && next <= MAX_MANA_AMOUNT) {
            setMana(itemStack, key, next);
            return true;
        }
        return false;
    }

    //常量getter
    public double getMaxMana() {return MAX_MANA_AMOUNT;}
    public int getMaxManaType() {return MAX_MANA_TYPE;}

    // 统一“回满魔力”的交互：仅创造模式生效
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isCreative = player.getAbilities().instabuild;

        if (!level.isClientSide()) {
            //if (isCreative) {
                refillAllTypesToMax(stack);
            //} 暂时去掉
        }

        return isCreative ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // 可复用：将四种魔力直接回满至 getMaxMana()
    protected void refillAllTypesToMax(ItemStack stack) {
        double max = getMaxMana();
        setMana(stack, "radiation", max);
        setMana(stack, "temperature", max);
        setMana(stack, "momentum", max);
        setMana(stack, "pressure", max);
    }
}
