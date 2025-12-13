package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.spells.SpellLogic;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServerPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:ServerPacketHandler");

    public static void handleSpellRelease(final SpellReleasePacket packet, final IPayloadContext context) {
        LOGGER.info("=== 服务端接收到法术释放数据包 ===");
        
        context.enqueueWork(() -> {
            try {
            Player player = context.player();
            double charge = packet.charge();

                // 从服务端读取“隐藏保存”优先，其次读取普通存储
                List<ItemStack> spells = new java.util.ArrayList<>();
                ItemStack held = player.getMainHandItem();
                List<ItemStack> saved = held.get(ModDataComponents.WAND_SAVED_STACKS.get());
                if (saved == null || saved.isEmpty()) {
                    saved = held.get(ModDataComponents.WAND_STACKS_SMALL.get());
                }
                if (saved == null || saved.isEmpty()) {
                    held = player.getOffhandItem();
                    saved = held.get(ModDataComponents.WAND_SAVED_STACKS.get());
                    if (saved == null || saved.isEmpty()) saved = held.get(ModDataComponents.WAND_STACKS_SMALL.get());
                }
                if (saved != null) for (ItemStack it : saved) {
                    if (it == null || it.isEmpty()) continue;
                    ItemStack cp = it.copy();
                    cp.setCount(1);
                    spells.add(cp);
                }

                LOGGER.info("处理法术释放请求 - 玩家: {}, 服务端读取法术数量: {}", 
                    player.getName().getString(), spells.size());

                LOGGER.info("开始创建 SpellLogic 实例");
            // 创建法术逻辑实例并执行（服务端构造序列）
            SpellLogic spellLogic = new SpellLogic(spells, player, charge);
                LOGGER.info("SpellLogic 实例创建成功，开始执行法术");
                
            spellLogic.execute();
                LOGGER.info("法术执行完成");

                LOGGER.info("法术释放处理完成");
                
            } catch (Exception e) {
                LOGGER.error("处理法术释放数据包时发生错误", e);
            }
        });
        
        LOGGER.debug("法术释放处理任务已加入服务端工作队列");
    }

    public static void handleWandMenuKV(final GuiDataPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof WandMenu menu) {
                menu.setClientData(packet.key(), packet.value());
            }
        });
    }
} 
