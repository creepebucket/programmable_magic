package org.creepebucket.programmable_magic.registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.logic.MananetChunkNodes;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;
import static net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES;

public final class ModAttachments {

    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<MananetChunkNodes>> CHUNK_NODES = ATTACHMENTS.register(
            "mananet_chunk_nodes",
            () -> AttachmentType.serializable(MananetChunkNodes::new).build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}

