package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeRegistrar;
import org.creepebucket.programmable_magic.mechines.api.BaseControllerBlockEntity;
import org.creepebucket.programmable_magic.mechines.api.UniversalMultiblockControllerBlock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class MultiblockController {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 在这里注册
    public static final DeferredBlock<UniversalMultiblockControllerBlock<BaseControllerBlockEntity>> DEMO_MULTIBLOCK_CONTROLLER = registerController(
            "demo_multiblock_controller",
            List.of(
                    List.of("AAA", "A#A", "AAA")
            ),
            Map.of('A', List.of("minecraft:stone"))
    );

    public static <BE extends BaseControllerBlockEntity> DeferredBlock<UniversalMultiblockControllerBlock<BE>> registerController(
            String controllerBlockId,
            List<List<String>> pattern,
            Map<Character, List<String>> blockMap,
            BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier) {

        DeferredBlock<UniversalMultiblockControllerBlock<BE>> controller = BLOCKS.register(controllerBlockId, registerName ->
                new UniversalMultiblockControllerBlock<>(
                        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registerName)),
                        pattern,
                        blockMap,
                        blockEntitySupplier
                ));

        ITEMS.registerSimpleBlockItem(controllerBlockId, controller::get);
        MananetNodeRegistrar.registerBlockEntity(BLOCK_ENTITIES, controllerBlockId, blockEntitySupplier, controller);
        return controller;
    }

    public static DeferredBlock<UniversalMultiblockControllerBlock<BaseControllerBlockEntity>> registerController(
            String controllerBlockId,
            List<List<String>> pattern,
            Map<Character, List<String>> blockMap) {

        AtomicReference<Supplier<BlockEntityType<BaseControllerBlockEntity>>> blockEntityTypeRef = new AtomicReference<>();
        BlockEntityType.BlockEntitySupplier<BaseControllerBlockEntity> blockEntitySupplier = (pos, state) ->
                new BaseControllerBlockEntity(blockEntityTypeRef.get().get(), pos, state, pattern, blockMap);

        DeferredBlock<UniversalMultiblockControllerBlock<BaseControllerBlockEntity>> controller = BLOCKS.register(controllerBlockId, registerName ->
                new UniversalMultiblockControllerBlock<>(
                        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registerName)),
                        pattern,
                        blockMap,
                        blockEntitySupplier
                ));

        ITEMS.registerSimpleBlockItem(controllerBlockId, controller::get);
        Supplier<BlockEntityType<BaseControllerBlockEntity>> blockEntityType = MananetNodeRegistrar.registerBlockEntity(BLOCK_ENTITIES, controllerBlockId, blockEntitySupplier, controller);
        blockEntityTypeRef.set(blockEntityType);
        return controller;
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
