package org.creepebucket.programmable_magic.mananet.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MananetNetworkSavedData extends SavedData {

    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    private static final Codec<Mana> MANA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("radiation").forGetter(Mana::getRadiation),
            Codec.DOUBLE.fieldOf("temperature").forGetter(Mana::getTemperature),
            Codec.DOUBLE.fieldOf("momentum").forGetter(Mana::getMomentum),
            Codec.DOUBLE.fieldOf("pressure").forGetter(Mana::getPressure)
    ).apply(instance, Mana::new));

    private static final Codec<Map<UUID, UUID>> PARENT_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
            map -> {
                Map<UUID, UUID> out = new HashMap<>();
                for (var entry : map.entrySet()) out.put(UUID.fromString(entry.getKey()), UUID.fromString(entry.getValue()));
                return out;
            },
            map -> {
                Map<String, String> out = new HashMap<>();
                for (var entry : map.entrySet()) out.put(entry.getKey().toString(), entry.getValue().toString());
                return out;
            }
    );

    private static final Codec<Map<UUID, Mana>> MANA_MAP_CODEC = Codec.unboundedMap(UUID_CODEC, MANA_CODEC);

    public static final SavedDataType<MananetNetworkSavedData> ID = new SavedDataType<>(
            "mananet/network",
            MananetNetworkSavedData::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    PARENT_CODEC.fieldOf("parent").forGetter(sd -> sd.parent),
                    MANA_MAP_CODEC.fieldOf("mana").forGetter(sd -> sd.manaById)
            ).apply(instance, MananetNetworkSavedData::new))
    );

    private final Map<UUID, UUID> parent = new HashMap<>();
    private final Map<UUID, Mana> manaById = new HashMap<>();

    public MananetNetworkSavedData() {}

    public MananetNetworkSavedData(Map<UUID, UUID> parent, Map<UUID, Mana> manaById) {
        if (parent != null) this.parent.putAll(parent);
        if (manaById != null) this.manaById.putAll(manaById);
    }

    public static MananetNetworkSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(ID);
    }

    public Map<UUID, UUID> parent() {
        return parent;
    }

    public Mana getMana(UUID id) {
        Mana mana = manaById.get(id);
        return mana != null ? ManaMath.copy(mana) : new Mana();
    }

    public void writeFromManager(MananetNetworkManager manager) {
        parent.clear();
        parent.putAll(manager.exportParent());
        manaById.clear();
        for (MananetNetworkManager.NetworkState state : manager.iterateNetworks()) {
            manaById.put(state.id, ManaMath.copy(state.mana));
        }
        setDirty();
    }
}

