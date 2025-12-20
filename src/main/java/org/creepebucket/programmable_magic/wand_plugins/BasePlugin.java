package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils.WandValues;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.List;

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
     * 屏幕初始化回调：用于创建按钮/开关等控件。
     */
    public abstract void screenStartupLogic(int x, int y, WandScreen screen);

    /**
     * 屏幕渲染回调：用于自定义绘制或状态提示。
     */
    public abstract void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen);

    /**
     * 屏幕每 tick 回调：用于在界面打开期间的周期性逻辑（如自动充能）。
     */
    public abstract void screenTick(int x, int y, WandScreen screen);

    /**
     * 菜单布局回调：用于添加槽位或根据屏幕坐标进行布局。
     */
    public abstract void menuLogic(int x, int y, WandMenu menu);

    /**
     * 菜单每 tick 回调：可用于定时刷新菜单状态。
     */
    public abstract void menuTick(int x, int y, WandMenu menu);

    /**
     * 法术执行前回调：可读取/修改将要执行的参数。
     */
    public abstract void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);

    /**
     * 法术执行后回调：根据执行结果进行后处理。
     */
    public abstract void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);

    /**
     * 数值调整器：用于由插件对魔杖相关数值进行调整。
     * - 默认不做修改；具体插件可按需覆盖。
     * - values 中的字段语义：
     *   - manaMult：魔力倍率（参与发射时 supply 计算，0 表示不生效）。
     *   - chargeRateW：充能功率（W）。
     *   - spellSlots：法术槽位数（仅供展示或后续扩展，不直接改容器大小）。
     *   - pluginSlots：插件槽位数（仅供展示或后续扩展，不直接改容器大小）。
     */
    public abstract void adjustWandValues(WandValues values, ItemStack pluginStack);
}
