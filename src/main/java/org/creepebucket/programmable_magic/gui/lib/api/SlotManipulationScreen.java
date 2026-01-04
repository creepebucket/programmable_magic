package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SlotManipulationScreen<Menu extends AbstractContainerMenu> extends AbstractContainerScreen<Menu> {

    public SlotManipulationScreen(Menu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY) {
        var pos = ClientSlotManager.getClientPosition(slot);
        int i = pos != null ? pos.getFirst() : slot.x;
        int j = pos != null ? pos.getSecond() : slot.y;
        ItemStack itemstack = slot.getItem();
        boolean flag = false;
        boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemStack1 = this.menu.getCarried();
        String s = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemStack1.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack1, true) && this.menu.canDragTo(slot)) {
                flag = true;
                int k = Math.min(itemStack1.getMaxStackSize(), slot.getMaxStackSize(itemStack1));
                int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int i1 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack1) + l;
                if (i1 > k) {
                    i1 = k;
                    String var10000 = ChatFormatting.YELLOW.toString();
                    s = var10000 + k;
                }

                itemstack = itemStack1.copyWithCount(i1);
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        if (itemstack.isEmpty() && slot.isActive()) {
            Identifier resourcelocation = slot.getNoItemIcon();
            if (resourcelocation != null) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, i, j, 16, 16);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                guiGraphics.fill(i, j, i + 16, j + 16, -2130706433);
            }

            this.renderSlotContents(guiGraphics, itemstack, slot, s);
        }

    }

    protected void renderSlotContents(@NotNull GuiGraphics guiGraphics, @NotNull ItemStack itemstack, @NotNull Slot slot, @Nullable String countString) {
        var pos = ClientSlotManager.getClientPosition(slot);
        int x = pos != null ? pos.getFirst() : slot.x;
        int y = pos != null ? pos.getSecond() : slot.y;
        int seed = x + y * this.imageWidth;
        if (slot.isFake()) {
            guiGraphics.renderFakeItem(itemstack,x,y,seed);
        } else {
            guiGraphics.renderItem(itemstack,x,y,seed);
        }
        Font font = IClientItemExtensions.of(itemstack).getFont(itemstack, IClientItemExtensions.FontContext.ITEM_COUNT);
        guiGraphics.renderItemDecorations(font != null ? font : this.font, itemstack, x, y, countString);
    }

    @Override
    protected boolean isHovering(@NotNull Slot slot, double mouseX, double mouseY) {
        var pos = ClientSlotManager.getClientPosition(slot);
        int x = pos != null ? pos.getFirst() : slot.x;
        int y = pos != null ? pos.getSecond() : slot.y;
        return this.isHovering(x, y, 16, 16, mouseX, mouseY);
    }

    @Override
    protected void renderSlotHighlightBack(@NotNull GuiGraphics guiGraphics) {
        if (this.hoveredSlot == null || !this.hoveredSlot.isHighlightable()) return;
        var pos = ClientSlotManager.getClientPosition(this.hoveredSlot);
        int x = pos != null ? pos.getFirst() : this.hoveredSlot.x;
        int y = pos != null ? pos.getSecond() : this.hoveredSlot.y;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, x - 4, y - 4, 24, 24);
    }

    @Override
    protected void renderSlotHighlightFront(@NotNull GuiGraphics guiGraphics) {
        if (this.hoveredSlot == null || !this.hoveredSlot.isHighlightable()) return;
        var pos = ClientSlotManager.getClientPosition(this.hoveredSlot);
        int x = pos != null ? pos.getFirst() : this.hoveredSlot.x;
        int y = pos != null ? pos.getSecond() : this.hoveredSlot.y;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24);
    }
}
