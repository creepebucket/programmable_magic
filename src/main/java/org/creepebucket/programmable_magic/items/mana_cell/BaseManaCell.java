package org.creepebucket.programmable_magic.items.mana_cell;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseManaCell extends Item {

    //魔力单元的属性
    private final int MAX_MANA_AMOUNT;
    private final int MAX_MANA_TYPE;

    public BaseManaCell(Properties properties, int maxManaAmount, int maxManaType) {super(properties);
        MAX_MANA_AMOUNT = maxManaAmount;
        MAX_MANA_TYPE = maxManaType;
    }


    //魔力getter/setter
    public Map<String, Integer> getManaMap(ItemStack itemStack) {
        Map<String, Integer> properties = itemStack.get(ModDataComponents.MANA.get());
        return properties != null ? properties : new HashMap<>();
    }

    public int getMana(ItemStack itemStack, String key) {
        Map<String, Integer> mana = getManaMap(itemStack);
        return mana.getOrDefault(key, 0);
    }

    public void setMana(ItemStack itemStack, String key, int value) {
        // 复制为可变映射，避免对不可变实现进行修改
        Map<String, Integer> mana = new HashMap<>(getManaMap(itemStack));
        mana.put(key, value);
        itemStack.set(ModDataComponents.MANA.get(), mana);
    }

    /*
    往单元添加魔力 value负数就减少
    返回false操作不成功(撞上下限了), 操作被取消
     */
    public boolean addMana(ItemStack itemStack, String key, int value) {
        Map<String, Integer> manaMap = getManaMap(itemStack);
        int current = getMana(itemStack, key);

        // 类型数量仅在正向新增一种新类型时检查
        if (value > 0) {
            int usedTypes = 0;
            for (int v : manaMap.values()) {
                if (v > 0) usedTypes++;
            }
            boolean isNewType = current == 0 && value > 0;
            if (isNewType && usedTypes >= MAX_MANA_TYPE) {
                return false;
            }
        }

        // 上下限检查
        int next = current + value;
        if (0 <= next && next <= MAX_MANA_AMOUNT) {
            setMana(itemStack, key, next);
            return true;
        }
        return false;
    }

    //常量getter
    public int getMaxMana() {return MAX_MANA_AMOUNT;}
    public int getMaxManaType() {return MAX_MANA_TYPE;}
}
