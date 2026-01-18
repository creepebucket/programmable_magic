package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollRegionWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SelectableImageButtonWidget;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandUiWidgets;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;
import org.creepebucket.programmable_magic.ModUtils.WandValues;

/**
 * 插件：左侧法术供应栏（分类选择 + 内容滚动）。
 * - screenStartupLogic：创建四个互斥侧栏切换按钮。
 * - screenRenderLogic：按分类绘制分组标题与 5 列网格槽背景。
 * - menuLogic：在菜单中构建可滚动映射的供应槽位并初始化映射。
 */
public class SpellSupply extends BasePlugin{
    private final int tier;
    public SpellSupply() { this(1); }
    public SpellSupply(int tier) { this.tier = Math.max(1, tier); this.pluginName = "spell_supply_t" + this.tier; }

    @Override
    /**
     * 实体 tick：本插件无实体侧行为。
     */
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    public void buildUi(WandMenu menu) {
        menu.ui().addWidget(new WandUiWidgets.SpellSupplyBackgroundWidget(menu));

        menu.ui().addWidget(new SelectableImageButtonWidget(Coordinate.fromTopLeft(0, 8),
                16, 48,
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute_pressed.png"),
                () -> "compute".equals(menu.selectedSidebar()),
                () -> menu.sendMenuData(WandMenu.KEY_SPELL_SIDEBAR, "compute")));

        menu.ui().addWidget(new SelectableImageButtonWidget(Coordinate.fromTopLeft(0, 48 + 8),
                16, 48,
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust_pressed.png"),
                () -> "adjust".equals(menu.selectedSidebar()),
                () -> menu.sendMenuData(WandMenu.KEY_SPELL_SIDEBAR, "adjust")));

        menu.ui().addWidget(new SelectableImageButtonWidget(Coordinate.fromTopLeft(0, 2 * 48 + 8),
                16, 48,
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control_pressed.png"),
                () -> "control".equals(menu.selectedSidebar()),
                () -> menu.sendMenuData(WandMenu.KEY_SPELL_SIDEBAR, "control")));

        menu.ui().addWidget(new SelectableImageButtonWidget(Coordinate.fromTopLeft(0, 3 * 48 + 8),
                16, 48,
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base_pressed.png"),
                () -> "base".equals(menu.selectedSidebar()),
                () -> menu.sendMenuData(WandMenu.KEY_SPELL_SIDEBAR, "base")));

        menu.ui().addWidget(new ScrollRegionWidget(Coordinate.fromTopLeft(17, 0),
                () -> 82,
                () -> Minecraft.getInstance().getWindow().getGuiScaledHeight() - 16,
                menu::supplyScrollRow,
                () -> computeMaxSupplyScrollRow(menu),
                v -> menu.sendMenuData(WandMenu.KEY_SUPPLY_SCROLL, v)));
    }

    private int computeMaxSupplyScrollRow(WandMenu menu) {
        SpellItemLogic.SpellType type = SpellUtils.stringSpellTypeMap.getOrDefault(menu.selectedSidebar(), SpellItemLogic.SpellType.COMPUTE_MOD);
        Map<Component, List<ItemStack>> spells = SpellUtils.getSpellsGroupedBySubCategory(type);

        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int visibleRows = Math.max(1, Math.floorDiv((sh - 16) - 20, 16));

        int totalRows = 1;
        for (Map.Entry<Component, List<ItemStack>> entry : spells.entrySet()) {
            int size = entry.getValue().size();
            int rows = (int) Math.ceil(size / 5.0);
            totalRows += rows + 1;
        }

        return Math.max(0, totalRows - visibleRows);
    }

    @Override
    /**
     * 执行前：不更改法术参数。
     */
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    /**
     * 执行后：不更改执行结果。
     */
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    /**
     * 数值调整：按等级降低魔力倍率（倍数相乘）。
     * 等级 = 物品堆叠数量；倍率因子 = 1 / (1 + 0.2 * 等级)。
     */
    public void adjustWandValues(WandValues values, net.minecraft.world.item.ItemStack pluginStack) {
        values.manaMult = 1 * Math.pow(0.95, this.tier - 1);
    }
}
