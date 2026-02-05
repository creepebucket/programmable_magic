package org.creepebucket.programmable_magic.network.dataPackets;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:ServerPacketHandler");

    public static void handleSpellRelease(final SpellReleasePacket packet, final IPayloadContext context) {
        LOGGER.info("=== 服务端接收到法术释放数据包 ===");
    }

    public static void handleWandMenuKV(final SimpleKvPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof SimpleKvC2SHandler handler) {
                handler.handleSimpleKvC2S(packet.key(), packet.value());
            }
        });
    }

    public static void handleHookTrigger(final HookTriggerPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof Menu menu) {
                menu.hooks.handleOnServer(packet.hookId(), context.player(), packet.args());
            }
        });
    }
} 
