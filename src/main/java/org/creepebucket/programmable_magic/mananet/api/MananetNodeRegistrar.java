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
import net.minecraft.resources.ResourceLocation;

public final class MananetNodeRegistrar {

    private MananetNodeRegistrar() {}

    public static <T extends Block> DeferredBlock<T> registerBlock(DeferredRegister.Blocks blocks, String id, Function<ResourceLocation, ? extends T> factory) {
        return blocks.register(id, factory);
    }

    public static DeferredItem<BlockItem> registerBlockItem(DeferredRegister.Items items, String id, Supplier<? extends Block> block) {
        return items.register(id, registryName -> new BlockItem(
                block.get(),
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
        ));
    }

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
