package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.creepebucket.programmable_magic.items.mana_cell.BaseManaCell;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;
import org.creepebucket.programmable_magic.spells.SpellCostCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandSpellStorageScreen extends BaseWandScreen<WandSpellStorageMenu> {
    private int SLOTS;

    public WandSpellStorageScreen(WandSpellStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    void renderCustomBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_STORAGE_BG, x + menu.spellStorageLeftX, y + menu.spellstorageY, 0, 0, menu.spellStorageWidth, 16, 448, 16);
    }
}
