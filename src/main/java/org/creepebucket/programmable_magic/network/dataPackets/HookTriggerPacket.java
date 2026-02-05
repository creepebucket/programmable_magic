package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public record HookTriggerPacket(String hookId, Object[] args) implements CustomPacketPayload {
    public static final Type<HookTriggerPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MODID, "hook_trigger"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HookTriggerPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public HookTriggerPacket decode(RegistryFriendlyByteBuf buf) {
            String hookId = ByteBufCodecs.STRING_UTF8.decode(buf);
            int size = ByteBufCodecs.VAR_INT.decode(buf);
            Object[] args = new Object[size];
            for (int i = 0; i < size; i++) args[i] = SimpleKvCodec.decodeValue(buf);
            return new HookTriggerPacket(hookId, args);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, HookTriggerPacket value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.hookId());
            ByteBufCodecs.VAR_INT.encode(buf, value.args().length);
            for (Object arg : value.args()) SimpleKvCodec.encodeValue(buf, arg);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

