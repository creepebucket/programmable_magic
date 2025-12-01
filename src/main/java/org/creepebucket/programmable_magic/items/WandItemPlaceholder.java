package org.creepebucket.programmable_magic.items;

import net.minecraft.world.item.Item;

/**
 * 魔杖物品占位符：
 * - 用于在法术序列中占一个“物品参数”的位置。
 * - 绑定逻辑与扣除在 SpellLogic 中执行。
 */
public class WandItemPlaceholder extends Item {
    public WandItemPlaceholder(Properties properties) {
        super(properties);
    }
}
