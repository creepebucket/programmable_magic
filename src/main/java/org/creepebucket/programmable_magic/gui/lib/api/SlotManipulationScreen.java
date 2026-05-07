package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public abstract class SlotManipulationScreen<Menu extends AbstractContainerMenu> extends AbstractContainerScreen<Menu> {

    public SlotManipulationScreen(Menu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (Slot slot : this.menu.slots) {
            var clientPos = ClientSlotManager.getClientPosition(slot);
            if (clientPos == null) continue;
            slot.x = clientPos.getFirst();
            slot.y = clientPos.getSecond();
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }
}
