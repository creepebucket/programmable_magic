package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.arcanism.mananet.connectors.BasicManaConnector;
import org.creepebucket.arcanism.mananet.connectors.HorizontalManaConnector;
import org.creepebucket.arcanism.mananet.connectors.VerticalManaConnector;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<BasicManaConnector> BASIC_MANA_CONNECTOR =
            BLOCKS.register("basic_mana_connector", registryName -> new BasicManaConnector(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    ModBlockEntities.BASIC_MANA_CONNECTOR_BLOCK_ENTITY));

    public static final DeferredBlock<HorizontalManaConnector> HORIZONTAL_MANA_CONNECTOR =
            BLOCKS.register("horizontal_mana_connector", registryName -> new HorizontalManaConnector(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    ModBlockEntities.HORIZONTAL_MANA_CONNECTOR_BLOCK_ENTITY));

    public static final DeferredBlock<VerticalManaConnector> VERTICAL_MANA_CONNECTOR =
            BLOCKS.register("vertical_mana_connector", registryName -> new VerticalManaConnector(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName)),
                    ModBlockEntities.VERTICAL_MANA_CONNECTOR_BLOCK_ENTITY));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
