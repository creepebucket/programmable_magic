package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.block.ManaCableBlock;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<ManaCableBlock> MANA_CABLE = BLOCKS.register("mana_cable",
            registryName -> new ManaCableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0f).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static void register(IEventBus bus) {BLOCKS.register(bus);}    
}


