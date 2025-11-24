package org.creepebucket.programmable_magic.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.Programmable_magic;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Programmable_magic.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, Double>>> MANA =
            DATA_COMPONENTS.registerComponentType("mana", builder -> builder
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.DOUBLE))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.STRING_UTF8,
                            ByteBufCodecs.DOUBLE
                    )));

    // 魔杖内置法术清单（分大/小两套）：按物品注册名存储
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> WAND_SPELLS_BIG =
            DATA_COMPONENTS.registerComponentType("wand_spells_big", builder -> builder
                    .persistent(Codec.list(Codec.STRING))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> WAND_SPELLS_SMALL =
            DATA_COMPONENTS.registerComponentType("wand_spells_small", builder -> builder
                    .persistent(Codec.list(Codec.STRING))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8)));

    public static void register(IEventBus eventBus) {DATA_COMPONENTS.register(eventBus);}
}
