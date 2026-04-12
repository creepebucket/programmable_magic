package org.creepebucket.programmable_magic.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(ATTACHMENT_TYPES, MODID);
    public static final Supplier<AttachmentType<ModUtils.Mana>> MANA = ATTACHMENTS.register(
            "mana",
            () -> AttachmentType.builder(ModUtils.Mana::new).serialize(ModUtils.Mana.CODEC.fieldOf("mana")).build()
    );

    public static final Supplier<AttachmentType<Map<Direction, BlockPos>>> CONNECTIONS = ATTACHMENTS.register(
            "connections",
            () -> AttachmentType.<Map<Direction, BlockPos>>builder(() -> new HashMap<>())
                    .serialize(Codec.unboundedMap(Direction.CODEC, BlockPos.CODEC).fieldOf("connections"))
                    .sync(new StreamCodec<>() {
                        @Override
                        public Map<Direction, BlockPos> decode(RegistryFriendlyByteBuf buf) {
                            int size = ByteBufCodecs.VAR_INT.decode(buf);
                            var map = new HashMap<Direction, BlockPos>(size);
                            for (int i = 0; i < size; i++) {
                                var direction = Direction.from3DDataValue(ByteBufCodecs.VAR_INT.decode(buf));
                                var pos = BlockPos.of(ByteBufCodecs.VAR_LONG.decode(buf));
                                map.put(direction, pos);
                            }
                            return map;
                        }

                        @Override
                        public void encode(RegistryFriendlyByteBuf buf, Map<Direction, BlockPos> value) {
                            ByteBufCodecs.VAR_INT.encode(buf, value.size());
                            for (var entry : value.entrySet()) {
                                ByteBufCodecs.VAR_INT.encode(buf, entry.getKey().get3DDataValue());
                                ByteBufCodecs.VAR_LONG.encode(buf, entry.getValue().asLong());
                            }
                        }
                    })
                    .build()
    );

    public static final Supplier<AttachmentType<BlockPos>> PENDING_CONNECTION = ATTACHMENTS.register(
            "pending_connection",
            () -> AttachmentType.builder(() -> BlockPos.ZERO).serialize(BlockPos.CODEC.fieldOf("pending_connection")).build()
    );

    public static final Supplier<AttachmentType<Direction>> PENDING_FACE = ATTACHMENTS.register(
            "pending_face",
            () -> AttachmentType.builder(() -> Direction.NORTH).serialize(Direction.CODEC.fieldOf("pending_face")).build()
    );

    public static final Supplier<AttachmentType<Long>> NETWORK_ID = ATTACHMENTS.register(
            "pm_mananet_id",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf("pm_mananet_id")).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
