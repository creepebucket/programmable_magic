package org.creepebucket.programmable_magic.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class SpellItemLogic {
    public SpellItemLogic() {}

    /**
     * 获取法术的注册名称
     */
    public abstract String getRegistryName();

    /**
     * 执行法术逻辑
     * @param player 施法者
     * @param data 法术数据（可修改）
     * @return true 表示继续处理下一个法术，false 表示下一tick再处理这个法术
     */
    public abstract boolean run(Player player, SpellData data);

    /**
     * 获取法术类型
     */
    public abstract SpellType getSpellType();

    /**
     * 计算基础魔力消耗
     * @param data 法术数据
     */
    public abstract void calculateBaseMana(SpellData data);

    /**
     * 应用魔力修正（用于adjust_mod等）
     * @param data 法术数据
     */
    public void applyManaModification(SpellData data) {
        // 默认不修改，子类可以重写
    }

    /**
     * 获取法术的Tooltip
     * @return Tooltip组件列表
     */
    public abstract List<Component> getTooltip();

    /**
     * 检查是否可以执行（魔力是否足够等）
     * @param player 施法者
     * @param data 法术数据
     * @return 是否可以执行
     */
    public boolean canExecute(Player player, SpellData data) {
        // 默认总是可以执行，子类可以重写添加条件检查
        return true;
    }

    /**
     * 法术类型枚举
     */
    public enum SpellType {
        BASE_SPELL,     // 基础法术效果
        ADJUST_MOD,     // 数值调整
        CONTROL_MOD,    // 控制调整
        COMPUTE_MOD     // 逻辑运算
    }
}
