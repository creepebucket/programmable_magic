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
import java.util.List;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Programmable_magic.MODID);

    // 占位符绑定：存储被绑定物品的注册名（ResourceLocation.toString）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PLACEHOLDER_ITEM_ID =
            DATA_COMPONENTS.registerComponentType("placeholder_item_id", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // 隐藏保存：供左键释放读取
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> SPELLS =
            DATA_COMPONENTS.registerComponentType("spells", builder -> builder
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> SAVED_PACKS =
            DATA_COMPONENTS.registerComponentType("saved_packs", builder -> builder
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    // 魔杖插件：玩家装配的插件物品列表（顺序即槽位顺序）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> PLUGINS =
            DATA_COMPONENTS.registerComponentType("plugins", builder -> builder
                    .persistent(Codec.list(ItemStack.CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    // 被动充能：安装自动充能插件时，手持时每tick累积的充能时长（以 tick 计）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AUTO_CHARGE_TICKS =
            DATA_COMPONENTS.registerComponentType("auto_charge_ticks", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT));

    // 上次释放结束的世界时间戳（tick）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> LAST_RELEASE_TIME =
            DATA_COMPONENTS.registerComponentType("last_release_time", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DESCRIPTION =
            DATA_COMPONENTS.registerComponentType("description", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> RESOURCE_LOCATION =
            DATA_COMPONENTS.registerComponentType("resource_location", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> AUTHER =
            DATA_COMPONENTS.registerComponentType("auther", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
