package org.creepebucket.programmable_magic.registries;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.creepebucket.programmable_magic.network.dataPackets.ServerPacketHandler;
import org.creepebucket.programmable_magic.network.dataPackets.SpellReleasePacket;
import org.creepebucket.programmable_magic.network.dataPackets.GuiDataPacket;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModNetworkPackets {
    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        // 获取注册器，"yourmodid" 应该换成你的 Mod ID
        final PayloadRegistrar registrar = event.registrar(MODID);

        // 注册一个从客户端发送到服务端的 Play-phase 数据包
        registrar.playToServer(
                SpellReleasePacket.TYPE,             // 数据包的唯一类型标识
                SpellReleasePacket.STREAM_CODEC,     // 数据包的序列化/反序列化逻辑
                ServerPacketHandler::handleSpellRelease // 服务端接收到数据包后要调用的方法
        );

        // Screen -> Menu: 传递一个键值对（k=string, v=object）
        registrar.playToServer(
                GuiDataPacket.TYPE,
                GuiDataPacket.STREAM_CODEC,
                ServerPacketHandler::handleWandMenuKV
        );

        // 如果你还有其他数据包，可以在这里继续注册...
        // registrar.playToClient(...); // 注册从服务端到客户端的数据包
        // registrar.playBidirectional(...); // 注册双向数据包
    }
}
