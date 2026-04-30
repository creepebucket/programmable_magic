package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.mechines.DummyBlock;
import org.creepebucket.programmable_magic.mananet.mechines.wind_turbine.WindTurbine;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class MananetNodeBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<WindTurbine> WIND_TURBINE =
            BLOCKS.register("wind_turbine", registryName -> new WindTurbine(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<DummyBlock> DUMMY_BLOCK =
            BLOCKS.register("dummy_block", registryName -> new DummyBlock(
                    BlockBehaviour.Properties.of().noOcclusion().instabreak().noLootTable().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredItem<BlockItem> WIND_TURBINE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(WIND_TURBINE);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
