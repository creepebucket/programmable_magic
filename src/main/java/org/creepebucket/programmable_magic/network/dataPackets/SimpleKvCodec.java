package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

public class SimpleKvCodec {

    public static Object decodeValue(RegistryFriendlyByteBuf buf) {
        int tag = ByteBufCodecs.VAR_INT.decode(buf);
        return switch (tag) {
            case 0 -> ByteBufCodecs.STRING_UTF8.decode(buf);
            case 1 -> ByteBufCodecs.VAR_INT.decode(buf);
            case 2 -> ByteBufCodecs.DOUBLE.decode(buf);
            case 3 -> ByteBufCodecs.BOOL.decode(buf);
            default -> throw new IllegalStateException("unknown value tag: " + tag);
        };
    }

    public static void encodeValue(RegistryFriendlyByteBuf buf, Object v) {
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
}

