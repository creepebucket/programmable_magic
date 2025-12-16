package org.creepebucket.programmable_magic.network.dataPackets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public record SpellReleasePacket(List<ItemStack> spells, double charge, List<ItemStack> plugins) implements CustomPacketPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellReleasePacket");

    public static final CustomPacketPayload.Type<SpellReleasePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "spell_release"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellReleasePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC),
            SpellReleasePacket::spells,
            ByteBufCodecs.DOUBLE,
            SpellReleasePacket::charge,
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC),
            SpellReleasePacket::plugins,
            SpellReleasePacket::new
    );

    public SpellReleasePacket(List<ItemStack> spells, double charge, List<ItemStack> plugins) {
        this.spells = spells;
        this.charge = charge;
        this.plugins = plugins;
        LOGGER.debug("SpellReleasePacket 构造函数被调用，包含 {} 个法术, charge={}", spells.size(), charge);
        for (int i = 0; i < spells.size(); i++) {
            ItemStack spell = spells.get(i);
            LOGGER.debug("法术 {}: {} x{}", i + 1, spell.getDisplayName().getString(), spell.getCount());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        LOGGER.debug("获取数据包类型: {}", TYPE.id());
        return TYPE;
    }
}
