package org.creepebucket.programmable_magic.wand_plugins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.wand.MathUtils;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 插件：左侧法术供应栏（分类选择 + 内容滚动）。
 * - screenStartupLogic：创建四个互斥侧栏切换按钮。
 * - screenRenderLogic：按分类绘制分组标题与 5 列网格槽背景。
 * - menuLogic：在菜单中构建可滚动映射的供应槽位并初始化映射。
 */
public class SpellSupply extends BasePlugin{
    public SpellSupply() { this.pluginName = "spell_supply"; }

    @Override
    /**
     * 实体 tick：本插件无实体侧行为。
     */
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    /**
     * 屏幕初始化：创建四个互斥的侧栏切换按钮。
     */
    public void screenStartupLogic(int x, int y, WandScreen screen) {
        // 侧栏（互斥）
        screen.sidebarCompute = new WandScreen.SidebarToggleWidget(0, 8, 16, 48,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute_pressed.png"),
                () -> screen.setSidebar("compute"));
        screen.sidebarAdjust = new WandScreen.SidebarToggleWidget(0, 48 + 8, 16, 48,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust_pressed.png"),
                () -> screen.setSidebar("adjust"));
        screen.sidebarControl = new WandScreen.SidebarToggleWidget(0, 2 * 48 + 8, 16, 48,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control_pressed.png"),
                () -> screen.setSidebar("control"));
        screen.sidebarBase = new WandScreen.SidebarToggleWidget(0, 3 * 48 + 8, 16, 48,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base_pressed.png"),
                () -> screen.setSidebar("base"));

        screen.addRenderableWidget(screen.sidebarCompute);
        screen.addRenderableWidget(screen.sidebarAdjust);
        screen.addRenderableWidget(screen.sidebarControl);
        screen.addRenderableWidget(screen.sidebarBase);
    }

    @Override
    /**
     * 屏幕渲染：绘制法术侧栏外框、分组标题与网格槽背景。
     */
    public void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sh = win.getGuiScaledHeight();

        // 法术侧栏
        guiGraphics.fill(17 - screen.getGuiLeft(), -screen.getGuiTop(), 17 - screen.getGuiLeft() + 1, sh - 16, 0xFFFFFFFF);
        guiGraphics.fill(17 + 82 - screen.getGuiLeft(), -screen.getGuiTop(), 17 + 82 - screen.getGuiLeft() + 1, sh - 16, 0xFFFFFFFF);

        // 左侧法术选择菜单
        Map<Component, List<ItemStack>> spells = SpellUtils.getSpellsGroupedBySubCategory(SpellUtils.stringSpellTypeMap.get(screen.sidebar));
        // 遍历每个键值对
        int startX = 19 - screen.getGuiLeft();
        int startY = 10 - screen.getGuiTop() - screen.supplyScrollRow * 16;

        x = startX;
        y = startY;

        for (Map.Entry<Component, List<ItemStack>> entry : spells.entrySet()) {
            Component key = entry.getKey();

            guiGraphics.drawString(screen.getFont(), key.getString(), x, y, 0xFFBF360C);

            for (int i = 0; i < entry.getValue().size(); i++) guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_sidebar_slot.png"),
                    x + (i % 5) * 16 - 1, y + Math.floorDiv(i, 5) * 16 + 10, 0, 0, 16, 16, 16, 16);

            y += Math.floorDiv(entry.getValue().size() - 1, 5) * 16 + 32;
        }
    }

    @Override
    /**
     * 菜单布局：固定 5 列可见网格，创建 SupplySlot 后初始化一次映射。
     */
    public void menuLogic(int x, int y, WandMenu menu) {
        menu.supplyItems = menu.computeSupplyItemsForCurrentSidebar();

        var win = Minecraft.getInstance().getWindow();
        int sh = win.getGuiScaledHeight();

        // 固定网格：从顶部 20px 开始，至底部上边距 16px，5 列
        int startX = 19; // 屏幕坐标（随后减去 gui_left）
        int startY = 4;
        int visibleHeightPx = sh - 4;
        int visibleRows = Math.max(1, Math.floorDiv(visibleHeightPx, 16));
        int total = visibleRows * 5;

        for (int i = 0; i < total; i++) {
            int col = i % 5;
            int row = Math.floorDiv(i, 5);
            int screenX = startX + col * 16 - 1;
            int screenY = startY + row * 16;
            var slot = menu.addSupplySlotConverted(-1, screenX, screenY);
            slot.setActive(false);
            menu.supplySlots.add(slot);
        }

        // 初始化一次映射
        menu.updateSupplySlotMapping();
    }

    @Override
    /**
     * 菜单 tick：本插件无菜单侧持续行为。
     */
    public void menuTick(int x, int y, WandMenu menu) {

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
}
