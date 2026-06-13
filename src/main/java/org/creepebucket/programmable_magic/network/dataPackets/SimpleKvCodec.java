package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

import org.creepebucket.programmable_magic.ModUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleKvCodec {

    public static Object decodeValue(RegistryFriendlyByteBuf buf) {
        int tag = ByteBufCodecs.VAR_INT.decode(buf);
        return switch (tag) {
            case 0 -> ByteBufCodecs.STRING_UTF8.decode(buf);
            case 1 -> ByteBufCodecs.VAR_INT.decode(buf);
            case 2 -> ByteBufCodecs.DOUBLE.decode(buf);
            case 3 -> ByteBufCodecs.BOOL.decode(buf);
            case 4 -> {
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                var list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) list.add(decodeValue(buf));
                yield list;
            }
            case 5 -> {
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                var map = new HashMap<Object, Object>(size);
                for (int i = 0; i < size; i++) {
                    Object k = decodeValue(buf);
                    map.put(k, decodeValue(buf));
                }
                yield map;
            }
            case 6 -> ByteBufCodecs.VAR_LONG.decode(buf);
            case 7 -> {
                double r = ByteBufCodecs.DOUBLE.decode(buf);
                double t = ByteBufCodecs.DOUBLE.decode(buf);
                double m = ByteBufCodecs.DOUBLE.decode(buf);
                double p = ByteBufCodecs.DOUBLE.decode(buf);
                yield new ModUtils.Mana(r, t, m, p);
            }
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
        } else if (v instanceof Long l) {
            ByteBufCodecs.VAR_INT.encode(buf, 6);
            ByteBufCodecs.VAR_LONG.encode(buf, l);
        } else if (v instanceof List<?> list) {
            ByteBufCodecs.VAR_INT.encode(buf, 4);
            ByteBufCodecs.VAR_INT.encode(buf, list.size());
            for (Object e : list) encodeValue(buf, e);
        } else if (v instanceof Map<?, ?> map) {
            ByteBufCodecs.VAR_INT.encode(buf, 5);
            ByteBufCodecs.VAR_INT.encode(buf, map.size());
            for (var entry : map.entrySet()) {
                encodeValue(buf, entry.getKey());
                encodeValue(buf, entry.getValue());
            }
        } else if (v instanceof ModUtils.Mana mana) {
            ByteBufCodecs.VAR_INT.encode(buf, 7);
            ByteBufCodecs.DOUBLE.encode(buf, mana.getRadiation());
            ByteBufCodecs.DOUBLE.encode(buf, mana.getTemperature());
            ByteBufCodecs.DOUBLE.encode(buf, mana.getMomentum());
            ByteBufCodecs.DOUBLE.encode(buf, mana.getPressure());
        } else {
            throw new IllegalArgumentException("unsupported value type: " + v);
        }
    }
}
