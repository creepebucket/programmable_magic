package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import java.util.function.Supplier;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.blockentity.ManaCableBlockEntity;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<ManaCableBlockEntity>> MANA_CABLE_BE = BLOCK_ENTITIES.register(
            "mana_cable",
            () -> new BlockEntityType<>(
                    ManaCableBlockEntity::new,
                    false,
                    ModBlocks.MANA_CABLE.get()
            )
    );

    public static void register(IEventBus bus) {BLOCK_ENTITIES.register(bus);}    
}


