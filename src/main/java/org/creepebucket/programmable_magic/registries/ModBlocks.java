package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mechines.ExampleUniversalMultiblockControllerBlock;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<ExampleUniversalMultiblockControllerBlock> EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER = BLOCKS.register(
            "example_universal_multiblock_controller",
            registryName -> new ExampleUniversalMultiblockControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
                    .setId(ResourceKey.create(Registries.BLOCK, registryName)))
    );

    public static void register(IEventBus bus) {BLOCKS.register(bus);}    
}
