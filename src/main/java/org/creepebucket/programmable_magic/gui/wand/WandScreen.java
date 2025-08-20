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
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;
import org.creepebucket.programmable_magic.spells.SpellCostCalculator;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandScreen extends BaseWandScreen<WandMenu> {

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
                    List<ItemStack> filtered = spells.stream()
                            .filter(s -> s != null && !s.isEmpty() && s.getCount() > 0)
                            .map(s -> {
                                ItemStack c = s.copy();
                                int clamped = Math.min(Math.max(c.getCount(), 1), 99);
                                c.setCount(clamped);
                                return c;
                            })
                            .collect(Collectors.toList());
                    if (!filtered.isEmpty()) {
                        ClientPacketDistributor.sendToServer(new SpellReleasePacket(filtered));
                    }
                },
                supplier -> supplier.get()
        ));

        SLOTS = menu.getSlots();
    }

    @Override
    void renderCustomBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPELL_STORAGE_BG, x + menu.spellStorageLeftX, y + menu.spellstorageY, 0, 0, menu.spellStorageWidth, 16, 448, 16);
        //背包
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INV_BG, x + BaseWamdMenu.playerInvLeftX + 1, y + BaseWamdMenu.hotbarY, 0, 0, BaseWamdMenu.playerInvWidth, BaseWamdMenu.playerInvHeight, BaseWamdMenu.playerInvWidth, BaseWamdMenu.playerInvHeight);
    }

    //TODO: 实现边拿边走
}
