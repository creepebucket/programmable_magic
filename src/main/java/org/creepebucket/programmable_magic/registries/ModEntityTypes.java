package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.entities.SpellEntity;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModEntityTypes {
    public static final DeferredRegister.Entities ENTITY_TYPES = 
            DeferredRegister.createEntities(MODID);
    
    public static final Supplier<EntityType<SpellEntity>> SPELL_ENTITY = ENTITY_TYPES.register(
            "spell_entity",
            () -> EntityType.Builder.<SpellEntity>of(SpellEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "spell_entity"))));
    
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
} 
