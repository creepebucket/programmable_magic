package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public record SimpleKvS2cPacket(String key, Object value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SimpleKvS2cPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "simple_kv_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SimpleKvS2cPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SimpleKvS2cPacket decode(RegistryFriendlyByteBuf buf) {
            String k = ByteBufCodecs.STRING_UTF8.decode(buf);
            Object v = SimpleKvCodec.decodeValue(buf);
            return new SimpleKvS2cPacket(k, v);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SimpleKvS2cPacket value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.key());
            SimpleKvCodec.encodeValue(buf, value.value());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

