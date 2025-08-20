package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.mana_cell.BaseManaCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.creepebucket.programmable_magic.spells.SpellCostCalculator;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class BaseWandScreen<M extends BaseWamdMenu> extends AbstractContainerScreen<M> {
    public static final ResourceLocation INV_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory_slot.png");
    public static final ResourceLocation SPELL_DISPLAY_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/spell_display_slot.png");
    public static final ResourceLocation SPELL_STORAGE_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/used_spell_slot.png");

    private int SLOTS;

    public BaseWandScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //取消黑色背景
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY); // fuck 这行代码没写浪费我5小时debug纪念 fuck
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        //法术槽位
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayLeftX, y + WandMenu.SpellDisplayTopY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayRightX, y + WandMenu.SpellDisplayTopY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayLeftX, y + WandMenu.SpellDisplayBottomY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayRightX, y + WandMenu.SpellDisplayBottomY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);

        renderCustomBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    abstract void renderCustomBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // 显示法术魔力需求与玩家可用魔力
        Player player = this.minecraft.player;
        if (player != null) {
            ManaInfo info = computeManaInfo(player, this.menu.getSpellsInStorage());

            int baseX = this.leftPos + 100;
            int baseY = this.topPos + 100;

            drawManaLine(guiGraphics, baseX, baseY + 0,  "辐射", info.required.get("radiation"), info.available.get("radiation"), 0xFFE1C542);
            drawManaLine(guiGraphics, baseX, baseY + 10, "温度", info.required.get("temperature"), info.available.get("temperature"), 0xFFE1534A);
            drawManaLine(guiGraphics, baseX, baseY + 20, "动量", info.required.get("momentum"), info.available.get("momentum"),   0xFF3A80E1);
            drawManaLine(guiGraphics, baseX, baseY + 30, "压力", info.required.get("pressure"), info.available.get("pressure"),    0xFF3CB371);
        }
    }

    private void drawManaLine(GuiGraphics g, int x, int y, String label, double need, int have, int labelColor) {
        boolean ok = have >= Math.ceil(need);
        int color = ok ? 0xFF55FF55 : 0xFFFF5555; // 绿/红
        String text = String.format("%s: 需要 %.1f / 拥有 %d", label, need, have);
        g.drawString(this.font, Component.literal(text), x, y, color, false);
    }

    private record ManaInfo(Map<String, Double> required, Map<String, Integer> available) {}

    private ManaInfo computeManaInfo(Player player, List<ItemStack> spellStacks) {
        Map<String, Double> required = SpellCostCalculator.computeRequiredManaFromStacks(spellStacks, player);

        // 统计可用魔力（遍历玩家背包里的 BaseManaCell）
        Map<String, Integer> available = new HashMap<>();
        available.put("radiation", 0);
        available.put("temperature", 0);
        available.put("momentum", 0);
        available.put("pressure", 0);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BaseManaCell cell) {
                available.computeIfPresent("radiation", (k, v) -> v + cell.getMana(stack, k));
                available.computeIfPresent("temperature", (k, v) -> v + cell.getMana(stack, k));
                available.computeIfPresent("momentum", (k, v) -> v + cell.getMana(stack, k));
                available.computeIfPresent("pressure", (k, v) -> v + cell.getMana(stack, k));
            }
        }

        return new ManaInfo(required, available);
    }

    //TODO: 实现边拿边走
}
