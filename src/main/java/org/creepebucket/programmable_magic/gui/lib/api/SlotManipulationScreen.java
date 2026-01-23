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

/**
 * 支持客户端槽位坐标重定位的容器界面基类。
 */
public abstract class SlotManipulationScreen<Menu extends AbstractContainerMenu> extends AbstractContainerScreen<Menu> {

    /**
     * 创建可操作槽位坐标的容器界面。
     */
    public SlotManipulationScreen(Menu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    /**
     * 以客户端坐标（若存在）渲染槽位内容。
     */
    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY) {
        // 计算 slot 的渲染坐标（若未设置则退回到原生 slot 坐标）
        int[] pos = slotPos(slot);
        int slotX = pos[0];
        int slotY = pos[1];

        // 准备待渲染的物品与渲染状态
        ItemStack stackToRender = slot.getItem();
        boolean showQuickCraftOverlay = false;
        boolean skipSlotContents = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack carriedStack = this.menu.getCarried();
        String countString = null;

        // 处理分割堆叠 / 快速合成拖拽时的数量显示逻辑
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !stackToRender.isEmpty()) {
            stackToRender = stackToRender.copyWithCount(stackToRender.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carriedStack.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) return;
            if (AbstractContainerMenu.canItemQuickReplace(slot, carriedStack, true) && this.menu.canDragTo(slot)) {
                showQuickCraftOverlay = true;
                int maxCount = Math.min(carriedStack.getMaxStackSize(), slot.getMaxStackSize(carriedStack));
                int existingCount = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int placeCount = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, carriedStack) + existingCount;
                if (placeCount > maxCount) {
                    placeCount = maxCount;
                    countString = ChatFormatting.YELLOW + Integer.toString(maxCount);
                }
                stackToRender = carriedStack.copyWithCount(placeCount);
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        // 空槽位：如果存在 no_item 图标则直接绘制该图标并跳过后续内容渲染
        if (stackToRender.isEmpty() && slot.isActive()) {
            Identifier noItemIcon = slot.getNoItemIcon();
            if (noItemIcon != null) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, noItemIcon, slotX, slotY, 16, 16);
                skipSlotContents = true;
            }
        }

        // 绘制快速合成的半透明覆盖，并渲染物品内容
        if (skipSlotContents) return;
        if (showQuickCraftOverlay) guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, -2130706433);
        this.renderSlotContents(guiGraphics, stackToRender, slot, countString);
    }

    /**
     * 绘制槽位内的物品与叠加文字。
     */
    protected void renderSlotContents(@NotNull GuiGraphics guiGraphics, @NotNull ItemStack itemstack, @NotNull Slot slot, @Nullable String countString) {
        int[] pos = slotPos(slot);
        int x = pos[0];
        int y = pos[1];
        int seed = x + y * this.imageWidth;
        if (slot.isFake()) {
            guiGraphics.renderFakeItem(itemstack, x, y, seed);
        } else {
            guiGraphics.renderItem(itemstack, x, y, seed);
        }
        Font font = IClientItemExtensions.of(itemstack).getFont(itemstack, IClientItemExtensions.FontContext.ITEM_COUNT);
        guiGraphics.renderItemDecorations(font != null ? font : this.font, itemstack, x, y, countString);
    }

    /**
     * 将 hover 判定与客户端槽位坐标对齐。
     */
    @Override
    protected boolean isHovering(@NotNull Slot slot, double mouseX, double mouseY) {
        int[] pos = slotPos(slot);
        return this.isHovering(pos[0], pos[1], 16, 16, mouseX, mouseY);
    }

    /**
     * 将 slot 高亮背景与客户端槽位坐标对齐。
     */
    @Override
    protected void renderSlotHighlightBack(@NotNull GuiGraphics guiGraphics) {
        Slot hoveredSlot = this.hoveredSlot;
        if (hoveredSlot == null || !hoveredSlot.isHighlightable()) return;
        int[] pos = slotPos(hoveredSlot);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, pos[0] - 4, pos[1] - 4, 24, 24);
    }

    /**
     * 将 slot 高亮前景与客户端槽位坐标对齐。
     */
    @Override
    protected void renderSlotHighlightFront(@NotNull GuiGraphics guiGraphics) {
        Slot hoveredSlot = this.hoveredSlot;
        if (hoveredSlot == null || !hoveredSlot.isHighlightable()) return;
        int[] pos = slotPos(hoveredSlot);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, pos[0] - 4, pos[1] - 4, 24, 24);
    }

    /**
     * 获取槽位的客户端渲染坐标（若未设置则使用原生 slot.x/y）。
     */
    private int[] slotPos(@NotNull Slot slot) {
        var pos = ClientSlotManager.getClientPosition(slot);
        if (pos != null) return new int[]{pos.getFirst(), pos.getSecond()};
        return new int[]{slot.x, slot.y};
    }
}
