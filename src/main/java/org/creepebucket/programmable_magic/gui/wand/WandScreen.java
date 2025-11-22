package org.creepebucket.programmable_magic.gui.wand;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.mana_cell.BaseManaCell;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;
import org.creepebucket.programmable_magic.spells.SpellCostCalculator;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandScreen extends AbstractContainerScreen<WandMenu> {
    private static final ResourceLocation INV_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory_slot.png");
    private static final ResourceLocation SPELL_DISPLAY_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/spell_display_slot.png");
    private static final ResourceLocation SPELL_STORAGE_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/used_spell_slot.png");

    private int SLOTS;

    public WandScreen(WandMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        final int CENTER_X = this.leftPos + 89;

        // 创建一个按钮
        this.addRenderableWidget(new ReleaseSpellButton(
                CENTER_X - 54, this.topPos + 90, 108, 26,
                Component.literal("Release Spell"),
                (button) -> {
                    // 在这里编写按钮被点击时执行的逻辑
                    var spells = this.menu.getSpellsInStorage();
                    if (!spells.isEmpty()) {
                        ClientPacketDistributor.sendToServer(new SpellReleasePacket(spells));
                    }
                },
                supplier -> supplier.get()
        ));

        SLOTS = menu.getSlots();
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

        //背包
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INV_BG, x + WandMenu.playerInvLeftX + 1, y + WandMenu.hotbarY, 0, 0, WandMenu.playerInvWidth, WandMenu.playerInvHeight, WandMenu.playerInvWidth, WandMenu.playerInvHeight);

        //法术槽位
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayLeftX, y + WandMenu.SpellDisplayTopY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayRightX, y + WandMenu.SpellDisplayTopY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayLeftX, y + WandMenu.SpellDisplayBottomY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_DISPLAY_BG, x + WandMenu.SpellDisplayRightX, y + WandMenu.SpellDisplayBottomY, 0, 0, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight, WandMenu.SpellDisplaySlotWidth, WandMenu.SpellDisplaySlotHeight);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_STORAGE_BG, x + WandMenu.spellStorageLeftX, y + WandMenu.spellstorageY, 0, 0, WandMenu.spellStorageWidth, 16, 448, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // 显示法术魔力需求与玩家可用魔力
        Player player = this.minecraft.player;
        if (player != null) {
            ManaInfo info = computeManaInfo(player, this.menu.getSpellsInStorage());

            int baseX =  WandMenu.spellStorageLeftX;
            int baseY =  WandMenu.spellstorageY + 20;

            drawManaLine(guiGraphics, baseX, baseY + 0,  "辐射", info.required.get("radiation"), info.available.get("radiation"), 0xFFE1C542);
            drawManaLine(guiGraphics, baseX, baseY + 10, "温度", info.required.get("temperature"), info.available.get("temperature"), 0xFFE1534A);
            drawManaLine(guiGraphics, baseX, baseY + 20, "动量", info.required.get("momentum"), info.available.get("momentum"),   0xFF3A80E1);
            drawManaLine(guiGraphics, baseX, baseY + 30, "压力", info.required.get("pressure"), info.available.get("pressure"),    0xFF3CB371);
        }
    }

    private void drawManaLine(GuiGraphics g, int x, int y, String label, double need, double have, int labelColor) {
        boolean ok = have >= need;
        int color = ok ? 0xFF55FF55 : 0xFFFF5555; // 绿/红
        String text = String.format("%s: 需要 %s / 拥有 %s", label,
                ModUtils.FormattedManaString(need), ModUtils.FormattedManaString(have));
        g.drawString(this.font, Component.literal(text), x, y, color, false);
    }

    private record ManaInfo(Map<String, Double> required, Map<String, Double> available) {}

    private ManaInfo computeManaInfo(Player player, List<ItemStack> spellStacks) {
        Map<String, Double> required = SpellCostCalculator.computeRequiredManaFromStacks(spellStacks, player);

        // 应用当前手持魔杖的魔力修正系数
        double manaMult = 1.0;
        try {
            var main = player.getMainHandItem().getItem();
            var off = player.getOffhandItem().getItem();
            if (main instanceof org.creepebucket.programmable_magic.items.wand.BaseWand w) {
                manaMult = Math.max(0.0, w.getManaMult());
            } else if (off instanceof org.creepebucket.programmable_magic.items.wand.BaseWand w2) {
                manaMult = Math.max(0.0, w2.getManaMult());
            }
        } catch (Exception ignored) {}
        if (manaMult != 1.0) {
            for (String k : required.keySet()) {
                required.put(k, required.get(k) * manaMult);
            }
        }

        // 统计可用魔力（遍历玩家背包里的 BaseManaCell）
        Map<String, Double> available = new HashMap<>();
        available.put("radiation", 0.0);
        available.put("temperature", 0.0);
        available.put("momentum", 0.0);
        available.put("pressure", 0.0);

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
