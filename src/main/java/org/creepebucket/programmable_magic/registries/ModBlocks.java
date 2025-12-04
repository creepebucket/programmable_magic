package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.block.PrimitiveAlloySmelterBlock;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<PrimitiveAlloySmelterBlock> PRIMITIVE_ALLOY_SMELTER = BLOCKS.register("primitive_alloy_smelter",
            registryName -> new PrimitiveAlloySmelterBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.0f)
                            .lightLevel(state -> state.hasProperty(PrimitiveAlloySmelterBlock.STATUS)
                                    && state.getValue(PrimitiveAlloySmelterBlock.STATUS) == PrimitiveAlloySmelterBlock.Status.BURN ? 13 : 0)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
            ));

    public static void register(IEventBus bus) {BLOCKS.register(bus);}    
}
