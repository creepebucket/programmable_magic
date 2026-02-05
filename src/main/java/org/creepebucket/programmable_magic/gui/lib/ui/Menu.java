package org.creepebucket.programmable_magic.gui.lib.ui;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.creepebucket.programmable_magic.gui.lib.api.DataManager;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.HookManager;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.slots.InfiniteSupplySlot;
import org.creepebucket.programmable_magic.gui.lib.widgets.SlotWidget;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvC2SHandler;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvS2CHandler;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvS2cPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Menu extends AbstractContainerMenu implements SimpleKvC2SHandler, SimpleKvS2CHandler {

    public final Inventory playerInv;
    public final DataManager dataManager = new DataManager();
    public final HookManager hooks = new HookManager();

    // 1. 控件列表现在放在 Menu 里，这样你在写 Menu 逻辑时就能塞控件进去了
    public List<Widget> widgets = new ArrayList<>();

    // 屏幕信息
    public int screenWidth;
    public int screenHeight;
    public int guiLeft;
    public int guiTop;

    protected Menu(MenuType<?> type, int containerId, Inventory playerInv, Definition definition) {
        super(type, containerId);
        this.playerInv = playerInv;
        this.hooks.bindMenuPlayer(playerInv.player);

        // 服务端发包逻辑
        if (playerInv.player instanceof ServerPlayer serverPlayer) {
            this.dataManager.bindClientSender((key, value) -> {
                var packet = new ClientboundCustomPayloadPacket(new SimpleKvS2cPacket(key, value));
                serverPlayer.connection.send(packet);
            });
        }

        // 2. 构建菜单（在这里面你可以调用 addWidget）
        definition.build(this);
    }

    // 傻瓜式添加控件方法
    public void addWidget(Widget widget) {
        this.widgets.add(widget);
        if (widget instanceof SlotWidget slotWidget) {
            this.addSlot(slotWidget.slot);
        }
    }

    public void addClientWidget(Supplier<Supplier<Widget>> supplier) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) addWidget(supplier.get().get());
    }

    // 3. 当 Screen 尺寸变化时，Screen 会调用这个方法
    // 我们在这里通知所有控件“初始化/位置更新”
    public void reportScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // 遍历所有控件，触发它们的初始化逻辑（通常用于重新计算位置）
        for (Widget widget : this.widgets) {
            if (widget instanceof Lifecycle lifecycle) {
                lifecycle.onInitialize();
            }
        }
    }

    public <T> SyncedValue<T> registerData(String key, SyncMode syncMode, T initialValue) {
        return this.dataManager.register(key, syncMode, initialValue);
    }

    public <T extends Hook> T hook(T hook) {
        return this.hooks.hook(hook);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void handleSimpleKvC2S(String key, Object value) {
        this.dataManager.handlePacket(key, value);
    }

    @Override
    public void handleSimpleKvS2C(String key, Object value) {
        this.dataManager.handlePacket(key, value);
    }

    public abstract void init();

    @FunctionalInterface
    public interface Definition {
        void build(Menu menu);
    }
}
