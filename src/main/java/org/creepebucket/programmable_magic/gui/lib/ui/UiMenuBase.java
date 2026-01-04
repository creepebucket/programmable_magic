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

/**
 * UI 菜单基类：负责绑定 KV 同步通道，并持有 {@link UiRuntime}。
 */
public abstract class UiMenuBase extends AbstractContainerMenu implements SimpleKvC2SHandler, SimpleKvS2CHandler {

    protected final Inventory playerInv;
    protected final UiRuntime ui = new UiRuntime();

    /**
     * 创建菜单并构建 UI 定义；服务端侧会自动绑定 s2c 发送通道。
     */
    protected UiMenuBase(MenuType<?> type, int containerId, Inventory playerInv, AbstractUi definition) {
        super(type, containerId);
        this.playerInv = playerInv;

        if (playerInv.player instanceof ServerPlayer sp) {
            this.ui.bindSendToClient((k, v) -> sp.connection.send(new ClientboundCustomPayloadPacket(new SimpleKvS2cPacket(k, v))));
        }

        definition.build(this.ui);
    }

    /**
     * 获取该菜单持有的 UI 运行时实例。
     */
    public UiRuntime ui() { return this.ui; }

    /**
     * UI 菜单默认始终有效；由上层自行决定关闭逻辑。
     */
    @Override
    public boolean stillValid(Player player) { return true; }

    /**
     * UI 菜单默认不支持快速搬运；需要时由子类实现。
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    /**
     * 接收客户端 -> 服务端的 KV 包并转交给 UI 运行时处理。
     */
    @Override
    public void handleSimpleKvC2S(String key, Object value) { this.ui.handleC2S(key, value); }

    /**
     * 接收服务端 -> 客户端的 KV 包并转交给 UI 运行时处理。
     */
    @Override
    public void handleSimpleKvS2C(String key, Object value) { this.ui.handleS2C(key, value); }
}
