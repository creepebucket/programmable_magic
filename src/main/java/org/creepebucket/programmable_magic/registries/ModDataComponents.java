package org.creepebucket.programmable_magic.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.Programmable_magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Programmable_magic.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, Integer>>> MANA =
            DATA_COMPONENTS.registerComponentType("mana", builder -> builder
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.STRING_UTF8,
                            ByteBufCodecs.INT
                    )));

    // 旧组件（兼容旧数据）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_SPELLS =
            DATA_COMPONENTS.registerComponentType("wand_spells", builder -> builder
                    .persistent(ItemStack.CODEC.listOf()));

    // 新组件：分别用于两个菜单，彼此独立
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_SPELLS_MENU =
            DATA_COMPONENTS.registerComponentType("wand_spells_menu", builder -> builder
                    .persistent(ItemStack.CODEC.listOf()));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_SPELLS_STORAGE =
            DATA_COMPONENTS.registerComponentType("wand_spells_storage", builder -> builder
                    .persistent(ItemStack.CODEC.listOf()));

    public static void register(IEventBus eventBus) {DATA_COMPONENTS.register(eventBus);}    
}
