package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.items.spell.BaseSpellItem;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.base_spell.ExplosionSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final Map<ResourceLocation, Supplier<SpellItemLogic>> LOGIC_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<SpellItemLogic>> REGISTERED_SPELLS = new HashMap<>();

    public static void registerSpells(IEventBus eventBus) {
        // 在这里注册所有法术

        // COMPUTE_MOD: 0-9
        registerSpell(() -> new NumberDigitSpell(0));
        registerSpell(() -> new NumberDigitSpell(1));
        registerSpell(() -> new NumberDigitSpell(2));
        registerSpell(() -> new NumberDigitSpell(3));
        registerSpell(() -> new NumberDigitSpell(4));
        registerSpell(() -> new NumberDigitSpell(5));
        registerSpell(() -> new NumberDigitSpell(6));
        registerSpell(() -> new NumberDigitSpell(7));
        registerSpell(() -> new NumberDigitSpell(8));
        registerSpell(() -> new NumberDigitSpell(9));

        // COMPUTE_MOD: 常量
        registerSpell(() -> new ValueLiteralSpell(SpellValueType.VECTOR3, "compute_unit_x", new Vec3(1.0, 0.0, 0.0),
                List.of(Component.translatable("item.programmable_magic.spell_display_compute_unit_x"))));
        registerSpell(DynamicConstantSpell.ViewVectorSpell::new);

        // COMPUTE_MOD: 分隔符
        registerSpell(SpellSeperator::new);

        // COMPUTE_MOD: 括号
        registerSpell(ParenSpell.LeftParenSpell::new);
        registerSpell(ParenSpell.RightParenSpell::new);

        // COMPUTE_MOD: 加减乘除幂
        registerSpell(MathOpreationsSpell.AdditionSpell::new);
        registerSpell(MathOpreationsSpell.SubtractionSpell::new);
        registerSpell(MathOpreationsSpell.MultiplicationSpell::new);
        registerSpell(MathOpreationsSpell.DivisionSpell::new);
        registerSpell(MathOpreationsSpell.PowerSpell::new);

        // 基础法术
        registerSpell(ExplosionSpell::new);

        ITEMS.register(eventBus);
    }

    private static void registerSpell(Supplier<SpellItemLogic> logicSupplier) {
        SpellItemLogic logicInstance = logicSupplier.get();
        String name = "spell_display_" + logicInstance.getRegistryName();
        Supplier<Item> itemSupplier = ITEMS.register(name,
                registryName -> new BaseSpellItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName)), logicInstance));
        
        LOGIC_SUPPLIERS.put(ResourceLocation.fromNamespaceAndPath(MODID, name), logicSupplier);
        REGISTERED_SPELLS.put(itemSupplier, logicSupplier);
    }

    public static SpellItemLogic createSpellLogic(Item item) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        Supplier<SpellItemLogic> supplier = LOGIC_SUPPLIERS.get(registryName);
        return supplier != null ? supplier.get() : null;
    }

    public static boolean isSpell(Item item) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        return LOGIC_SUPPLIERS.containsKey(registryName);
    }

    public static Map<Supplier<Item>, Supplier<SpellItemLogic>> getRegisteredSpells() {
        return REGISTERED_SPELLS;
    }
} 
