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

public class SpellSupply extends BasePlugin{
    public SpellSupply() { this.name = "spell_supply"; }

    @Override
    public void onEntityTick(SpellEntity spellEntity) {

    }

    @Override
    public void screenStartupLogic(int x, int y, WandScreen screen) {
        // 侧栏（互斥）
        var computeTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute.png");
        var computePressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute_pressed.png");
        var adjustTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust.png");
        var adjustPressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust_pressed.png");
        var controlTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control.png");
        var controlPressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control_pressed.png");
        var baseTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base.png");
        var basePressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base_pressed.png");

        screen.sidebarCompute = new WandScreen.SidebarToggleWidget(0, 8, 16, 48, computeTex, computePressedTex, () -> screen.setSidebar("compute"));
        screen.sidebarAdjust = new WandScreen.SidebarToggleWidget(0, 48 + 8, 16, 48, adjustTex, adjustPressedTex, () -> screen.setSidebar("adjust"));
        screen.sidebarControl = new WandScreen.SidebarToggleWidget(0, 2 * 48 + 8, 16, 48, controlTex, controlPressedTex, () -> screen.setSidebar("control"));
        screen.sidebarBase = new WandScreen.SidebarToggleWidget(0, 3 * 48 + 8, 16, 48, baseTex, basePressedTex, () -> screen.setSidebar("base"));

        screen.addRenderableWidget(screen.sidebarCompute);
        screen.addRenderableWidget(screen.sidebarAdjust);
        screen.addRenderableWidget(screen.sidebarControl);
        screen.addRenderableWidget(screen.sidebarBase);
    }

    @Override
    public void screenRenderLogic(GuiGraphics guiGraphics, int x, int y, WandScreen screen) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = screen.width / 2 - 1 - screen.getGuiLeft();
        int BOTTOM_Y = sh - screen.getGuiTop();

        // 法术侧栏
        guiGraphics.fill(17 - screen.getGuiLeft(), -screen.getGuiTop(), 17 - screen.getGuiLeft() + 1, sh - 16, 0xFFFFFFFF);
        guiGraphics.fill(17 + 82 - screen.getGuiLeft(), -screen.getGuiTop(), 17 + 82 - screen.getGuiLeft() + 1, sh - 16, 0xFFFFFFFF);

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        boolean compactMode = spellSlotCount <= 16;
        int compactModeYOffset = compactMode ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

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
    public void menuTick(int x, int y, WandMenu menu) {

    }

    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }

    @Override
    public void afterSpellExecution(SpellUtils.StepResult result, SpellEntity spellEntity, SpellItemLogic currentSpell, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {

    }
}
