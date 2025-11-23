package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.items.spell.BaseSpellItem;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.adjust_mod.PowerBoostSpell;
import org.creepebucket.programmable_magic.spells.base_spell.ExplosionSpell;
import org.creepebucket.programmable_magic.spells.base_spell.PaintDataSpell;
import org.creepebucket.programmable_magic.spells.base_spell.VelocitySpell;
import org.creepebucket.programmable_magic.spells.base_spell.IgniteSpell;
import org.creepebucket.programmable_magic.spells.base_spell.GlowSpell;
import org.creepebucket.programmable_magic.spells.adjust_mod.DelaySpell;
import org.creepebucket.programmable_magic.spells.control_mod.TriggerTouchGroundSpell;
import org.creepebucket.programmable_magic.spells.control_mod.TriggerTouchEntitySpell;
import org.creepebucket.programmable_magic.spells.compute_mod.AdditionSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.BuildXYZVectorSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.CasterEntitySpell;
import org.creepebucket.programmable_magic.spells.compute_mod.CasterPositionSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.CloseParenSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeDelimiterSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.DivisionSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.ExponentSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.GetStorageSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.MultiplicationSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.NumberComputeBase;
import org.creepebucket.programmable_magic.spells.compute_mod.OpenParenSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.SetStorageSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.SpellEntityPositionSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.SubtractionSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.UnitXVectorSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.UnitYVectorSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.UnitZVectorSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.ViewVectorSpell;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final Map<ResourceLocation, Supplier<SpellItemLogic>> LOGIC_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<SpellItemLogic>> REGISTERED_SPELLS = new HashMap<>();

    public static void registerSpells(IEventBus eventBus) {
        // 在这里注册所有法术
        registerSpell(ExplosionSpell::new);
        registerSpell(VelocitySpell::new);
        registerSpell(IgniteSpell::new);
        registerSpell(GlowSpell::new);
        registerSpell(PowerBoostSpell::new);
        registerSpell(DelaySpell::new);
        registerSpell(TriggerTouchGroundSpell::new);
        registerSpell(TriggerTouchEntitySpell::new);
        registerSpell(PaintDataSpell::new);

        // 计算类：数字（在父类文件内收集）
        for (var supplier : NumberComputeBase.allNumberSuppliers()) {
            registerSpell(supplier);
        }

        // 计算类：运算符（在父类外单独注册）
        registerSpell(AdditionSpell::new);
        registerSpell(SubtractionSpell::new);
        registerSpell(MultiplicationSpell::new);
        registerSpell(DivisionSpell::new);
        registerSpell(ExponentSpell::new);
        registerSpell(OpenParenSpell::new);
        registerSpell(CloseParenSpell::new);
        // 表达式分隔符（占位）
        registerSpell(ComputeDelimiterSpell::new);
        // 计算类：上下文/向量/存储工具
        registerSpell(CasterEntitySpell::new);
        registerSpell(CasterPositionSpell::new);
        registerSpell(SpellEntityPositionSpell::new);
        registerSpell(ViewVectorSpell::new);
        registerSpell(UnitXVectorSpell::new);
        registerSpell(UnitYVectorSpell::new);
        registerSpell(UnitZVectorSpell::new);
        registerSpell(SetStorageSpell::new);
        registerSpell(GetStorageSpell::new);
        registerSpell(BuildXYZVectorSpell::new);

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
