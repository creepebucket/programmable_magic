package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.items.BaseSpellItem;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.old.base_spell.ExplosionSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.VelocitySpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.PaintDataSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.ApplyPotionSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.BreakBlockSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.IgniteSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.ProjectileAttachSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.PlaceBlockSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.SendToInventorySpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.SpawnParticlesSpell;
import org.creepebucket.programmable_magic.spells.old.base_spell.TeleportSpell;
import org.creepebucket.programmable_magic.spells.old.compute_mod.*;
import org.creepebucket.programmable_magic.spells.old.compute_mod.EntityVelocitySpell;
import org.creepebucket.programmable_magic.spells.old.adjust_mod.ConditionInverter;
import org.creepebucket.programmable_magic.spells.old.adjust_mod.DelaySpell;
import org.creepebucket.programmable_magic.spells.old.adjust_mod.TriggerSpell;
import org.creepebucket.programmable_magic.spells.old.control_mod.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final Map<Identifier, Supplier<SpellItemLogic>> LOGIC_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<SpellItemLogic>> REGISTERED_SPELLS = new LinkedHashMap<>();

    public static void registerSpells(IEventBus eventBus) {
        // 在这里注册所有法术

        ITEMS.register(eventBus);
    }

    private static void registerSpell(Supplier<SpellItemLogic> logicSupplier) {
        SpellItemLogic logicInstance = logicSupplier.get();
        String name = "spell_display_" + logicInstance.name;
        Supplier<Item> itemSupplier = ITEMS.register(name,
                registryName -> new BaseSpellItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName)), logicInstance));
        
        LOGIC_SUPPLIERS.put(Identifier.fromNamespaceAndPath(MODID, name), logicSupplier);
        REGISTERED_SPELLS.put(itemSupplier, logicSupplier);
    }

    public static SpellItemLogic createSpellLogic(Item item) {
        Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
        Supplier<SpellItemLogic> supplier = LOGIC_SUPPLIERS.get(registryName);
        return supplier != null ? supplier.get() : null;
    }

    public static boolean isSpell(Item item) {
        Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
        return LOGIC_SUPPLIERS.containsKey(registryName);
    }

    public static Map<Supplier<Item>, Supplier<SpellItemLogic>> getRegisteredSpells() {
        return REGISTERED_SPELLS;
    }
} 
