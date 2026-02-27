package org.creepebucket.programmable_magic.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    .build()
    );

    public static final Supplier<AttachmentType<BlockPos>> PENDING_CONNECTION = ATTACHMENTS.register(
            "pending_connection",
            () -> AttachmentType.builder(() -> BlockPos.ZERO).serialize(BlockPos.CODEC.fieldOf("pending_connection")).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
