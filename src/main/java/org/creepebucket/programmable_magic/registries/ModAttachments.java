package org.creepebucket.programmable_magic.registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.logic.MananetChunkNodes;

import java.util.function.Supplier;

import static net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(ATTACHMENT_TYPES, MODID);
    public static final Supplier<AttachmentType<MananetChunkNodes>> CHUNK_NODES = ATTACHMENTS.register(
            "mananet_chunk_nodes",
            () -> AttachmentType.serializable(MananetChunkNodes::new).build()
    );
    public static final Supplier<AttachmentType<ModUtils.Mana>> MANA = ATTACHMENTS.register(
            "mana",
            () -> AttachmentType.builder(ModUtils.Mana::new).serialize(ModUtils.Mana.CODEC.fieldOf("mana")).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
