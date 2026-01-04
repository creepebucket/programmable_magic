package org.creepebucket.programmable_magic.gui.lib.ui;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvC2SHandler;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvS2CHandler;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvS2cPacket;

public abstract class UiMenuBase extends AbstractContainerMenu implements SimpleKvC2SHandler, SimpleKvS2CHandler {

    protected final Inventory playerInv;
    protected final UiRuntime ui = new UiRuntime();

    protected UiMenuBase(MenuType<?> type, int containerId, Inventory playerInv, AbstractUi definition) {
        super(type, containerId);
        this.playerInv = playerInv;

        if (playerInv.player instanceof ServerPlayer sp) {
            this.ui.bindSendToClient((k, v) -> sp.connection.send(new ClientboundCustomPayloadPacket(new SimpleKvS2cPacket(k, v))));
        }

        definition.build(this.ui);
    }

    public UiRuntime ui() { return this.ui; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public void handleSimpleKvC2S(String key, Object value) { this.ui.handleC2S(key, value); }

    @Override
    public void handleSimpleKvS2C(String key, Object value) { this.ui.handleS2C(key, value); }
}

