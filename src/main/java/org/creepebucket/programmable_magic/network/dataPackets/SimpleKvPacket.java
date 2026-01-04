package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 从客户端 Screen 向服务器端当前菜单传递一个键值对（k=string, v=object）。
 * 值支持的最小集合：string / int / double / boolean（保持实现简单直接）。
 */
public record SimpleKvPacket(String key, Object value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SimpleKvPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "wand_menu_kv"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SimpleKvPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SimpleKvPacket decode(RegistryFriendlyByteBuf buf) {
            String k = ByteBufCodecs.STRING_UTF8.decode(buf);
            Object v = SimpleKvCodec.decodeValue(buf);
            return new SimpleKvPacket(k, v);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SimpleKvPacket value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.key());
            SimpleKvCodec.encodeValue(buf, value.value());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
