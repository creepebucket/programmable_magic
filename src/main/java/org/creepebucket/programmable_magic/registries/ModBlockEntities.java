package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import java.util.function.Supplier;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mechines.ExampleUniversalMultiblockControllerBlockEntity;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<ExampleUniversalMultiblockControllerBlockEntity>> EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER =
            BLOCK_ENTITIES.register("example_universal_multiblock_controller",
                    () -> new BlockEntityType<>(ExampleUniversalMultiblockControllerBlockEntity::new, false, ModBlocks.EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER.get()));

    public static void register(IEventBus bus) {BLOCK_ENTITIES.register(bus);}    
}
