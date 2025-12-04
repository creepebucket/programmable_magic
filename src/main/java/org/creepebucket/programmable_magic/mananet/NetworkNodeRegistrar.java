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
import java.util.function.Supplier;

/**
 * 基于 AbstractNetworkNode 的“一键注册”工具：
 * - 注册方块(NodeBoundBlock) + 方块实体(NodeBoundBlockEntity) + 可选方块物品。
 * - BE ticker 自动回调到 AbstractNetworkNode#tick。
 */
public final class NetworkNodeRegistrar {
    private NetworkNodeRegistrar() {}

    public static void register(AbstractNetworkNode node) {
        NodeRegistryData meta = Objects.requireNonNull(node.getRegistryData());

        @SuppressWarnings("unchecked")
        final Supplier<BlockEntityType<NodeBoundBlockEntity>>[] beRef = new Supplier[]{() -> null};

        DeferredBlock<NodeBoundBlock> block = ModBlocks.BLOCKS.register(meta.name(),
                registryName -> {
                    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, registryName);
                    Block.Properties props = Block.Properties.of().setId(key);
                    return new NodeBoundBlock(props, node, () -> beRef[0].get());
                }
        );

        Supplier<BlockEntityType<NodeBoundBlockEntity>> beType = ModBlockEntities.BLOCK_ENTITIES.register(
                meta.name(),
                () -> new BlockEntityType<>((pos, state) -> new NodeBoundBlockEntity(beRef[0].get(), pos, state, node), false, block.get())
        );
        beRef[0] = beType;

        if (meta.withBlockItem()) {
            ModItems.ITEMS.register(meta.name(), registryName -> new BlockItem(
                    block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
            ));
        }
    }
}
