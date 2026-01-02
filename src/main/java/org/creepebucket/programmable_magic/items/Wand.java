package org.creepebucket.programmable_magic.items;

import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.spells.SpellLogic;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.Nullable;

/**
 * 最小魔杖基类：
 * - 右键（use）在服务端打开菜单，客户端由已注册的 Screen 渲染。
 * - 暴露法术倍率、法术槽位数、充能功率（W）、插件槽位数四个属性供工具提示与法术逻辑使用。
 */
public class Wand extends BowItem implements IItemExtension {

    private final int slots;
    private final int pluginSlots;

    private static boolean hasAutoChargePlugin(ItemStack stack) {
        java.util.List<ItemStack> plugins = stack.get(ModDataComponents.WAND_PLUGINS.get());
        if (plugins == null) return false;
        for (ItemStack it : plugins) {
            if (it == null || it.isEmpty()) continue;
            var id = BuiltInRegistries.ITEM.getKey(it.getItem());
            if (id != null && "wand_plugin_auto_charge".equals(id.getPath())) return true;
        }
        return false;
    }

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
     * 右键使用：潜行时在服务端打开菜单；否则进入“使用”态以进行按住充能。
     */
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            stack.set(ModDataComponents.WAND_LAST_RELEASE_TIME.get(), level.getGameTime());
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

        // 非潜行：进入“使用”态（按住右键充能，松手释放）
        ItemStack stack = player.getItemInHand(hand);
        if (!hasAutoChargePlugin(stack)) stack.set(ModDataComponents.WAND_LAST_RELEASE_TIME.get(), level.getGameTime());
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    /**
     * 使用期间每 tick 回调：客户端显示 HUD 能量条。
     */
    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof Player player)) return;

        if (level.isClientSide) {
            int total = getUseDuration(stack, living);
            int holdUsed = Math.max(0, total - remainingUseDuration);
            Long last = stack.get(ModDataComponents.WAND_LAST_RELEASE_TIME.get());
            long now = level.getGameTime();
            if (last == null) last = now;
            long dt = Math.max(0L, now - last);
            int used = Math.max(holdUsed, (int) Math.max(0L, dt));

            double rate = ModUtils.computeWandValues(stack.get(ModDataComponents.WAND_PLUGINS.get())).chargeRateW;
            double mana = (used / 20.0) * (rate / 1000.0);
            String bar = "|>>> " + ModUtils.FormattedManaString(mana) + " <<<|";

            for(int i = 1; i < 5; i++) {
                double n = Math.random() * 2 * Math.PI;
                double x_offset = Math.sin(n) * 0.5;
                double z_offset = Math.cos(n) * 0.5;

                player.displayClientMessage(net.minecraft.network.chat.Component.literal(bar), true);
                level.addParticle(new TrailParticleOption(player.getPosition(0).add(x_offset, 0, z_offset), 0xFFFFFFFF, 20),
                        player.getX() + x_offset,
                        player.getEyeY() + 2,
                        player.getZ() + z_offset, 0, 0, 0);
            }
        }
    }

    /**
     * 松开使用：仅在服务端计算充能时间并释放法术。
     */
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (level.isClientSide) return false;
        if (!(living instanceof Player player)) return false;
        if (player.isShiftKeyDown()) return false; // 潜行用于打开 GUI，不触发快捷充能释放

        int total = getUseDuration(stack, living);
        int holdUsed = Math.max(0, total - timeLeft);
        long now = level.getGameTime();
        Long last = stack.get(ModDataComponents.WAND_LAST_RELEASE_TIME.get());
        if (last == null) last = now;
        long dt = Math.max(0L, now - last);
        int used = (int) Math.max(0L, dt);
        double chargeSec = ((double) used) / 20.0;

        // 从魔杖组件重建法术与插件列表（与服务端数据包处理一致）
        java.util.List<ItemStack> spells = new java.util.ArrayList<>();
        {
            java.util.List<ItemStack> saved = stack.get(ModDataComponents.WAND_SAVED_STACKS.get());
            if (saved == null || saved.isEmpty()) saved = stack.get(ModDataComponents.WAND_STACKS_SMALL.get());
            if (saved != null) for (ItemStack it : saved) {
                if (it == null || it.isEmpty()) continue;
                ItemStack cp = it.copy();
                cp.setCount(1);
            //player.getInventory().setChanged();
                spells.add(cp);
            }
        }

        java.util.List<ItemStack> plugins = new java.util.ArrayList<>();
        {
            java.util.List<ItemStack> list = stack.get(ModDataComponents.WAND_PLUGINS.get());
            if (list != null) for (ItemStack it : list) { if (it != null && !it.isEmpty()) plugins.add(it.copy()); }
        }

        SpellLogic logic = new SpellLogic(spells, player, chargeSec, plugins);
        logic.execute();

        // 清空被动充能计数
        // 改为记录“上次释放结束”的世界时间戳（tick）
        stack.set(ModDataComponents.WAND_LAST_RELEASE_TIME.get(), now);
        return true;
    }

    /**
     * 允许长按使用（默认 1 小时，足够长）。
     */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }

    /**
     * 物品每 tick：若安装自动充能插件且被玩家手持，则累积被动充能时间；客户端在未按住使用时显示 HUD。
     */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof Player player)) return;

        boolean isHeld = player.isHolding(stack.getItem());
        if (!isHeld) return;

        java.util.List<ItemStack> plugins = stack.get(ModDataComponents.WAND_PLUGINS.get());
        if (!hasAutoChargePlugin(stack)) return;

        if (level.isClientSide) {
            if (player.containerMenu instanceof WandMenu) return;
            boolean using = player.isUsingItem() && (player.getUseItem() == stack);
            if (using) return; // 按住使用时由 onUseTick 负责 HUD

            double rate = ModUtils.computeWandValues(plugins).chargeRateW;

            Long last = stack.get(ModDataComponents.WAND_LAST_RELEASE_TIME.get());
            long now = level.getGameTime();
            if (last == null) last = now;
            long dt = Math.max(0L, now - last);
            double mana = (dt / 20.0) * (rate / 1000.0);
            String bar = "|>>> " + ModUtils.FormattedManaString(mana) + " <<<|";
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(bar), true);
        }
    }
}
