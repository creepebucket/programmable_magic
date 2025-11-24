package org.creepebucket.programmable_magic.network.wand;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.spells.SpellLogic;
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
            List<ItemStack> spells = packet.spells();

                LOGGER.info("处理法术释放请求 - 玩家: {}, 法术数量: {}", 
                    player.getName().getString(), spells.size());
                
                // 记录每个法术的详细信息
                for (int i = 0; i < spells.size(); i++) {
                    ItemStack spell = spells.get(i);
                    LOGGER.info("法术 {}: {} x{}", i + 1, spell.getDisplayName().getString(), spell.getCount());
                }
                
                LOGGER.info("开始创建 SpellLogic 实例");
            // 创建法术逻辑实例并执行
            SpellLogic spellLogic = new SpellLogic(spells, player);
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
} 
