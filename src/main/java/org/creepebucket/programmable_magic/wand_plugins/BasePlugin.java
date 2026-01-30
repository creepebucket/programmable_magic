package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;
import java.util.Map;

/**
 * 魔杖插件基类：定义插件在实体/界面/菜单/执行阶段的插入点。
 * - 注册名派生自字段 pluginName（前缀为 "wand_plugin_"）。
 * - 屏幕相关回调用于渲染与控件创建；菜单相关回调用于布局与状态同步。
 * - 执行相关回调在法术步骤执行前后触发，可读取并影响执行数据。
 */
public abstract class BasePlugin {
    /**
     * 插件名称（用于注册名后缀），例如：spell_supply。
     */
    public String pluginName;

    /**
     * 获取用于注册的路径名："wand_plugin_" + plugin_name。
     */
    public String getRegistryName() {
        return "wand_plugin_".concat(pluginName);
    }

    /**
     * 实体每 tick 回调：可用于持续效果或可视化。
     */
    public abstract void onEntityTick(SpellEntity spellEntity);

    /**
     * 构建魔杖界面：用于创建槽位布局与按钮/渲染等控件。
     */
    public abstract void buildUi(WandMenu menu);

    /**
     * 法术执行前回调：可读取/修改将要执行的参数。
     */
    public abstract void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams);

    /**
     * 法术执行后回调：根据执行结果进行后处理。
     */
    public abstract void afterSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, Map<String, Object> spellData, SpellSequence spellSequence, List<Object> spellParams);

    /**
     * 数值调整器：用于由插件对魔杖相关数值进行调整。
     * - 默认不做修改；具体插件可按需覆盖。
     * - values 中的字段语义：
     * - manaMult：魔力倍率（参与发射时 supply 计算，0 表示不生效）。
     * - chargeRateW：充能功率（W）。
     * - spellSlots：法术槽位数（仅供展示或后续扩展，不直接改容器大小）。
     * - pluginSlots：插件槽位数（仅供展示或后续扩展，不直接改容器大小）。
     */
    public abstract void adjustWandValues(WandValues values, ItemStack pluginStack);
}
