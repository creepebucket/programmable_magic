package org.creepebucket.programmable_magic.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;

/**
 * 最小魔杖基类：
 * - 右键（use）在服务端打开菜单，客户端由已注册的 Screen 渲染。
 * - 暴露法术倍率、法术槽位数、充能功率（W）、插件槽位数四个属性供工具提示与法术逻辑使用。
 */
public class Wand extends Item {

    private final int slots;
    private final int pluginSlots;

    /**
     * 构造一个魔杖实例。
     * @param properties 物品属性
     * @param slots 法术槽位最大数量（实际有效容量由插件控制）
     * @param pluginSlots 插件槽位最大数量
     */
    public Wand(Properties properties, int slots, int pluginSlots) {
        super(properties);
        this.slots = slots;
        this.pluginSlots = pluginSlots;
    }

    /**
     * 获取法术槽位数量。
     */
    public int getSlots() { return slots; }
    /**
     * 获取插件槽位数量。
     */
    public int getPluginSlots() { return pluginSlots; }

    @Override
    /**
     * 右键使用：仅在服务端打开菜单容器（Screen 由客户端绑定）。
     */
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, p) -> new WandMenu(containerId, inventory, hand),
                            Component.literal("")
                    ),
                    buf -> buf.writeVarInt(hand.ordinal())
            );
        }
        return InteractionResult.SUCCESS;
    }
}
