package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.mana_cell.BaseManaCell;
import org.creepebucket.programmable_magic.ModUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.creepebucket.programmable_magic.items.wand.BaseWand;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandScreen extends AbstractContainerScreen<WandMenu> {
    private static final ResourceLocation INV_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory_slot.png");
    private static final ResourceLocation SPELL_DISPLAY_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/spell_display_slot.png");
    private static final ResourceLocation SPELL_STORAGE_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/used_spell_slot.png");

    private int SLOTS;
    // 按住释放按钮充能
    private ReleaseSpellButton releaseButton;
    private boolean chargingButton = false;
    private long buttonPressStartNs = 0L;
    private static final int BTN_DECOR_EXTEND = 79; // 贴图左右扩展像素，用于扩大点击热区与视觉一致

    public WandScreen(WandMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        final int CENTER_X = this.leftPos + 89;
        // 普通界面保留按钮，小槽界面（潜行右键）不显示按钮
        if (!isSmallMode()) {
            this.releaseButton = this.addRenderableWidget(new ReleaseSpellButton(
                    CENTER_X - 54, this.topPos + 90, 108, 26,
                    Component.literal("Release Spell"),
                    (button) -> {
                        // 改为按住释放，不在 onPress 中发送
                    },
                    supplier -> supplier.get()
            ));
        }

        SLOTS = menu.getSlots();
    }

    private boolean isSmallMode() { return this.menu.isSmallMode(); }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //取消黑色背景
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY); // fuck 这行代码没写浪费我5小时debug纪念 fuck
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        // 按钮充能文本进度条
        if (!isSmallMode() && releaseButton != null) {
            // 兜底：若左键已按下且鼠标位于按钮可视区域内，但未触发mouseClicked，则在此处进入充能状态
            boolean leftDown = net.minecraft.client.Minecraft.getInstance().mouseHandler.isLeftPressed();
            if (leftDown && !chargingButton && isInReleaseArea(mouseX, mouseY)) {
                chargingButton = true;
                buttonPressStartNs = System.nanoTime();
            }
            if (chargingButton) {
                double secs = Math.max(0.0, (System.nanoTime() - buttonPressStartNs) / 1_000_000_000.0);
                drawChargeBar(guiGraphics, secs, this.leftPos + 89, this.topPos + 118);
            }
        }
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
    }


    //TODO: 实现边拿边走

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isSmallMode() && releaseButton != null && button == 0 && isInReleaseArea(mouseX, mouseY)) {
            chargingButton = true;
            buttonPressStartNs = System.nanoTime();
            return true; // 拦截，按住开始充能
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (chargingButton && button == 0) {
            chargingButton = false;
            double seconds = Math.max(0.0, (System.nanoTime() - buttonPressStartNs) / 1_000_000_000.0);
            // 发送带充能时间的数据包
            var spells = this.menu.getSpellsInStorage();
            if (!spells.isEmpty()) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new SpellReleasePacket(spells, seconds)));
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static final double MAX_SECONDS = 3.0;
    private void drawChargeBar(GuiGraphics g, double secs, int ignoreCenterX, int ignoreY) {
        // 根据手持魔杖的充能速率计算魔力量
        double rate = 0.0;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var main = player.getMainHandItem();
            var off = player.getOffhandItem();
            if (main.getItem() instanceof org.creepebucket.programmable_magic.items.wand.BaseWand w) rate = w.getChargeRate();
            else if (off.getItem() instanceof org.creepebucket.programmable_magic.items.wand.BaseWand w2) rate = w2.getChargeRate();
        }
        double mana = (rate * secs) / 1000.0;
        String text = String.format(java.util.Locale.ROOT, "charging %.2f mana", mana);
        int w = this.font.width(text);
        int x = this.width / 2 - w / 2;
        int y = this.height - 30;
        g.drawString(this.font, text, x, y, 0xFFFFFF, false);
        if (player != null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(text), true);
        }
    }

    private boolean isInReleaseArea(double mouseX, double mouseY) {
        int lx = this.releaseButton.getX() - BTN_DECOR_EXTEND;
        int rx = lx + this.releaseButton.getWidth() + BTN_DECOR_EXTEND * 2;
        int ty = this.releaseButton.getY();
        int by = ty + this.releaseButton.getHeight();
        return mouseX >= lx && mouseX <= rx && mouseY >= ty && mouseY <= by;
    }
}
