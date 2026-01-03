package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.Function;
import net.minecraft.resources.Identifier;

/**
 * Mananet 相关方块/物品/方块实体的注册辅助。
 *
 * <p>该类只做“薄封装”，目标是把常用的注册样板集中到一个地方，避免在各个节点类型里重复拼装参数。</p>
 */
public final class MananetNodeRegistrar {

    private MananetNodeRegistrar() {}

    /**
     * 注册节点方块（或其它方块）。
     *
     * @param blocks NeoForge 的方块延迟注册器
     * @param id 资源 id（不含命名空间）
     * @param factory 使用 registryName 构造方块实例的工厂
     */
    public static <T extends Block> DeferredBlock<T> registerBlock(DeferredRegister.Blocks blocks, String id, Function<Identifier, ? extends T> factory) {
        return blocks.register(id, factory);
    }

    /**
     * 注册与方块绑定的 {@link BlockItem}。
     *
     * <p>这里显式设置 {@link Item.Properties#setId(ResourceKey)}，使 1.21.8 的注册链在 item 层拥有稳定的 id。</p>
     */
    public static DeferredItem<BlockItem> registerBlockItem(DeferredRegister.Items items, String id, Supplier<? extends Block> block) {
        return items.register(id, registryName -> new BlockItem(
                block.get(),
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
        ));
    }

    /**
     * 注册方块实体类型，并绑定到多个方块（通常是一组共享同一方块实体逻辑的方块）。
     *
     * <p>注意：这里直接使用 {@link BlockEntityType} 构造器，保持注册过程扁平直观。</p>
     */
    @SafeVarargs
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            DeferredRegister<BlockEntityType<?>> blockEntities,
            String id,
            BlockEntityType.BlockEntitySupplier<T> supplier,
            Supplier<? extends Block>... blocks
    ) {
        return blockEntities.register(id, () -> new BlockEntityType<>(
                supplier,
                false,
                java.util.Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)
        ));
    }
}
