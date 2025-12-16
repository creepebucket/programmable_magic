package org.creepebucket.programmable_magic.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.Programmable_magic;
import net.minecraft.world.item.ItemStack;

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

    // 占位符绑定：存储被绑定物品的注册名（ResourceLocation.toString）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> WAND_PLACEHOLDER_ITEM_ID =
            DATA_COMPONENTS.registerComponentType("wand_placeholder_item_id", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // 直接存储完整 ItemStack（含数据组件/自定义状态）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_STACKS_BIG =
            DATA_COMPONENTS.registerComponentType("wand_stacks_big", builder -> builder
                    .persistent(Codec.list(ItemStack.CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_STACKS_SMALL =
            DATA_COMPONENTS.registerComponentType("wand_stacks_small", builder -> builder
                    .persistent(Codec.list(ItemStack.CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));
    
    // 隐藏保存：供左键释放读取
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_SAVED_STACKS =
            DATA_COMPONENTS.registerComponentType("wand_saved_stacks", builder -> builder
                    .persistent(Codec.list(ItemStack.CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    // 法术卷轴：存储一个条目列表，用于在解析法术时展开为实际条目
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> SPELL_SCROLL_STACKS =
            DATA_COMPONENTS.registerComponentType("spell_scroll_stacks", builder -> builder
                    .persistent(Codec.list(ItemStack.CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    // 魔杖插件：玩家装配的插件物品列表（顺序即槽位顺序）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> WAND_PLUGINS =
            DATA_COMPONENTS.registerComponentType("wand_plugins", builder -> builder
                    .persistent(Codec.list(ItemStack.CODEC))
                    .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)));

    public static void register(IEventBus eventBus) {DATA_COMPONENTS.register(eventBus);}
}
