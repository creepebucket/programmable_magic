package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.registries.ModBlocks;
import org.creepebucket.programmable_magic.registries.ModItems;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 基于 AbstractNetworkNode 的“一键注册”工具：
 * - 注册方块(NodeBoundBlock) + 方块实体(NodeBoundBlockEntity) + 可选方块物品。
 * - BE ticker 自动回调到 AbstractNetworkNode#tick。
 */
public final class NetworkNodeRegistrar {
    private NetworkNodeRegistrar() {}

    public static <T extends AbstractNetworkNode> void register(NodeRegistryData meta,
                                                                BiFunction<Block.Properties, Supplier<BlockEntityType<NodeBoundBlockEntity>>, T> factory) {
        Objects.requireNonNull(factory);
        NodeRegistryData data = Objects.requireNonNull(meta);

        @SuppressWarnings("unchecked")
        final Supplier<BlockEntityType<NodeBoundBlockEntity>>[] beRef = new Supplier[]{() -> null};

        DeferredBlock<T> block = ModBlocks.BLOCKS.register(data.name(),
                registryName -> {
                    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, registryName);
                    Block.Properties props = Block.Properties.of().setId(key);
                    return factory.apply(props, () -> beRef[0].get());
                }
        );

        Supplier<BlockEntityType<NodeBoundBlockEntity>> beType = ModBlockEntities.BLOCK_ENTITIES.register(
                data.name(),
                () -> new BlockEntityType<>((pos, state) -> new NodeBoundBlockEntity(beRef[0].get(), pos, state), false, block.get())
        );
        beRef[0] = beType;

        if (data.withBlockItem()) {
            ModItems.ITEMS.register(data.name(), registryName -> new BlockItem(
                    block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
            ));
        }
    }
}
