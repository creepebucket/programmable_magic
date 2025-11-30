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

    public WandScreen(WandMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        final int CENTER_X = this.leftPos + 89;
        // 普通界面保留按钮，小槽界面（潜行右键）不显示按钮
        if (!isSmallMode()) {
            this.addRenderableWidget(new ReleaseSpellButton(
                    CENTER_X - 54, this.topPos + 90, 108, 26,
                    Component.literal("Release Spell"),
                    (button) -> {
                        var spells = this.menu.getSpellsInStorage();
                        if (!spells.isEmpty()) {
                            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new SpellReleasePacket(spells)));
                        }
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
    }


    //TODO: 实现边拿边走
