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
public record GuiDataPacket(String key, Object value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GuiDataPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "wand_menu_kv"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GuiDataPacket decode(RegistryFriendlyByteBuf buf) {
            String k = ByteBufCodecs.STRING_UTF8.decode(buf);
            Object v = decodeValue(buf);
            return new GuiDataPacket(k, v);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, GuiDataPacket value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.key());
            encodeValue(buf, value.value());
        }

        private Object decodeValue(RegistryFriendlyByteBuf buf) {
            int tag = ByteBufCodecs.VAR_INT.decode(buf);
            return switch (tag) {
                case 0 -> ByteBufCodecs.STRING_UTF8.decode(buf);
                case 1 -> ByteBufCodecs.VAR_INT.decode(buf);
                case 2 -> ByteBufCodecs.DOUBLE.decode(buf);
                case 3 -> ByteBufCodecs.BOOL.decode(buf);
                default -> throw new IllegalStateException("unknown value tag: " + tag);
            };
        }

        private void encodeValue(RegistryFriendlyByteBuf buf, Object v) {
            if (v instanceof String s) {
                ByteBufCodecs.VAR_INT.encode(buf, 0);
                ByteBufCodecs.STRING_UTF8.encode(buf, s);
            } else if (v instanceof Integer i) {
                ByteBufCodecs.VAR_INT.encode(buf, 1);
                ByteBufCodecs.VAR_INT.encode(buf, i);
            } else if (v instanceof Double d) {
                ByteBufCodecs.VAR_INT.encode(buf, 2);
                ByteBufCodecs.DOUBLE.encode(buf, d);
            } else if (v instanceof Boolean b) {
                ByteBufCodecs.VAR_INT.encode(buf, 3);
                ByteBufCodecs.BOOL.encode(buf, b);
            } else {
                throw new IllegalArgumentException("unsupported value type: " + v);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
