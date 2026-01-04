package org.creepebucket.programmable_magic.network.dataPackets;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {
    public static void handleSimpleKvS2C(final SimpleKvS2cPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof SimpleKvS2CHandler handler) {
                handler.handleSimpleKvS2C(packet.key(), packet.value());
            }
        });
    }
}

