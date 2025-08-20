package org.creepebucket.programmable_magic.network.wand;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
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

public record SpellReleasePacket(List<ItemStack> spells) implements CustomPacketPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellReleasePacket");

    public static final CustomPacketPayload.Type<SpellReleasePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "spell_release"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellReleasePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC),
            SpellReleasePacket::spells,
            SpellReleasePacket::new
    );

    // 规范化构造器：过滤空物品并限制数量范围，防止编码时抛错
    public SpellReleasePacket {
        List<ItemStack> normalized = new ArrayList<>();
        for (ItemStack stack : spells) {
            if (stack == null || stack.isEmpty() || stack.getCount() <= 0) continue;
            ItemStack copy = stack.copy();
            int clamped = Math.min(Math.max(copy.getCount(), 1), 99);
            copy.setCount(clamped);
            normalized.add(copy);
        }
        spells = List.copyOf(normalized);
    }

    public SpellReleasePacket(List<ItemStack> spells, boolean log) {
        this(spells);
        if (log) {
            LOGGER.debug("SpellReleasePacket 构造函数被调用，包含 {} 个法术", this.spells.size());
            for (int i = 0; i < this.spells.size(); i++) {
                ItemStack spell = this.spells.get(i);
                LOGGER.debug("法术 {}: {} x{}", i + 1, spell.getDisplayName().getString(), spell.getCount());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        LOGGER.debug("获取数据包类型: {}", TYPE.id());
        return TYPE;
    }
}
