package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.items.BaseSpellItem;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.spells_adjust.TriggerSpell;
import org.creepebucket.programmable_magic.spells.spells_base.VisualEffectSpell;
import org.creepebucket.programmable_magic.spells.spells_compute.*;
import org.creepebucket.programmable_magic.spells.spells_control.BoolOperationsSpell;
import org.creepebucket.programmable_magic.spells.spells_control.FlowControlSpell;

import java.util.*;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SpellRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final Map<String, List<Supplier<Item>>> SPELLS_BY_SUBCATEGORY = new LinkedHashMap<>();
    private static final Map<Identifier, Supplier<SpellItemLogic>> LOGIC_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<SpellItemLogic>> REGISTERED_SPELLS = new LinkedHashMap<>();

    public static void registerSpells(IEventBus eventBus) {
        // 在这里注册所有法术

        // 数字和常量
        registerSpell(NumberDigitSpell.NumberDigit0::new);
        registerSpell(NumberDigitSpell.NumberDigit1::new);
        registerSpell(NumberDigitSpell.NumberDigit2::new);
        registerSpell(NumberDigitSpell.NumberDigit3::new);
        registerSpell(NumberDigitSpell.NumberDigit4::new);
        registerSpell(NumberDigitSpell.NumberDigit5::new);
        registerSpell(NumberDigitSpell.NumberDigit6::new);
        registerSpell(NumberDigitSpell.NumberDigit7::new);
        registerSpell(NumberDigitSpell.NumberDigit8::new);
        registerSpell(NumberDigitSpell.NumberDigit9::new);

        registerSpell(ValueLiteralSpell.PiSpell::new);

        registerSpell(ValueLiteralSpell.XUnitVectorSpell::new);
        registerSpell(ValueLiteralSpell.YUnitVectorSpell::new);
        registerSpell(ValueLiteralSpell.ZUnitVectorSpell::new);

        registerSpell(ValueLiteralSpell.TrueSpell::new);
        registerSpell(ValueLiteralSpell.FalseSpell::new);

        // 数字运算
        registerSpell(NumberOperationsSpell.AdditionSpell::new);
        registerSpell(NumberOperationsSpell.SubtractionSpell::new);
        registerSpell(NumberOperationsSpell.MultiplicationSpell::new);
        registerSpell(NumberOperationsSpell.DivisionSpell::new);
        registerSpell(NumberOperationsSpell.RemainderSpell::new);
        registerSpell(NumberOperationsSpell.ExponentSpell::new);

        registerSpell(NumberOperationsSpell.SinSpell::new);
        registerSpell(NumberOperationsSpell.CosSpell::new);
        registerSpell(NumberOperationsSpell.TanSpell::new);
        registerSpell(NumberOperationsSpell.AsinSpell::new);
        registerSpell(NumberOperationsSpell.AcosSpell::new);
        registerSpell(NumberOperationsSpell.AtanSpell::new);

        registerSpell(NumberOperationsSpell.CeilSpell::new);
        registerSpell(NumberOperationsSpell.FloorSpell::new);

        registerSpell(NumberOperationsSpell.RandomNumberSpell::new);

        registerSpell(NumberOperationsSpell.VectorLengthSpell::new);
        registerSpell(NumberOperationsSpell.VectorXSpell::new);
        registerSpell(NumberOperationsSpell.VectorYSpell::new);
        registerSpell(NumberOperationsSpell.VectorZSpell::new);

        registerSpell(NumberOperationsSpell.EntityArmorSpell::new);
        registerSpell(NumberOperationsSpell.EntityHealthSpell::new);
        registerSpell(NumberOperationsSpell.EntityMaxHealthSpell::new);

        // 动态常量

        registerSpell(DynamicConstantSpell.TimestampSpell::new);

        registerSpell(DynamicConstantSpell.CameraDirectionSpell::new);
        registerSpell(DynamicConstantSpell.CasterPositionSpell::new);
        registerSpell(DynamicConstantSpell.SpellPositionSpell::new);

        registerSpell(DynamicConstantSpell.CasterEntitySpell::new);
        registerSpell(DynamicConstantSpell.SpellEntitySpell::new);
        registerSpell(DynamicConstantSpell.NearestEntitySpell::new);

        // 其他计算
        registerSpell(BlockOperationsSpell.BlockPositionSpell::new);
        registerSpell(ParenSpell.LParenSpell::new);
        registerSpell(ParenSpell.RParenSpell::new);
        registerSpell(CommaSpell::new);
        registerSpell(StorageSpell.SetStoreSpell::new);
        registerSpell(StorageSpell.GetStoreSpell::new);
        registerSpell(Vector3OperationsSpell.BuildVectorSpell::new);
        registerSpell(Vector3OperationsSpell.EntityPositionSpell::new);
        registerSpell(Vector3OperationsSpell.EntityVelocitySpell::new);

        // 逻辑控制
        registerSpell(BoolOperationsSpell.GreaterThanSpell::new);
        registerSpell(BoolOperationsSpell.LessThanSpell::new);
        registerSpell(BoolOperationsSpell.EqualToSpell::new);
        registerSpell(BoolOperationsSpell.GreaterEqualToSpell::new);
        registerSpell(BoolOperationsSpell.LessEqualToSpell::new);
        registerSpell(BoolOperationsSpell.NotEqualToSpell::new);
        registerSpell(BoolOperationsSpell.AndSpell::new);
        registerSpell(BoolOperationsSpell.OrSpell::new);
        registerSpell(BoolOperationsSpell.NotSpell::new);
        registerSpell(BoolOperationsSpell.BlockIsAirSpell::new);
        registerSpell(BoolOperationsSpell.BlockHasGravitySpell::new);

        registerSpell(FlowControlSpell.LoopStartSpell::new);
        registerSpell(FlowControlSpell.ForLoopSpell::new);
        registerSpell(FlowControlSpell.LoopEndSpell::new);
        registerSpell(FlowControlSpell.BreakSpell::new);
        registerSpell(FlowControlSpell.ContinueSpell::new);
        registerSpell(FlowControlSpell.IfStartSpell::new);
        registerSpell(FlowControlSpell.IfEndSpell::new);
        registerSpell(FlowControlSpell.StopSpell::new);
        registerSpell(FlowControlSpell.RestartSpell::new);

        // 调整法术
        registerSpell(TriggerSpell.ConditionInvertSpell::new);
        registerSpell(TriggerSpell.TouchGroundSpell::new);
        registerSpell(TriggerSpell.TouchEntitySpell::new);
        registerSpell(TriggerSpell.DelaySpell::new);

        // 基础法术
        registerSpell(VisualEffectSpell.DebugPrintSpell::new);

        reorderSpellsBySubcategory();
        ITEMS.register(eventBus);
    }

    private static void reorderSpellsBySubcategory() {
        LinkedHashMap<String, List<Supplier<Item>>> reordered = new LinkedHashMap<>();
        for (String key : ModUtils.SPELL_COLORS().keySet()) {
            List<Supplier<Item>> value = SPELLS_BY_SUBCATEGORY.get(key);
            if (value != null) {
                reordered.put(key, value);
            }
        }
        for (Map.Entry<String, List<Supplier<Item>>> entry : SPELLS_BY_SUBCATEGORY.entrySet()) {
            if (!reordered.containsKey(entry.getKey())) {
                reordered.put(entry.getKey(), entry.getValue());
            }
        }

        SPELLS_BY_SUBCATEGORY.clear();
        SPELLS_BY_SUBCATEGORY.putAll(reordered);
    }

    private static void registerSpell(Supplier<SpellItemLogic> logicSupplier) {
        SpellItemLogic logicInstance = logicSupplier.get();
        String name = "spell_display_" + logicInstance.name;
        Supplier<Item> itemSupplier = ITEMS.register(name,
                registryName -> new BaseSpellItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName)), logicInstance));

        LOGIC_SUPPLIERS.put(Identifier.fromNamespaceAndPath(MODID, name), logicSupplier);
        REGISTERED_SPELLS.put(itemSupplier, logicSupplier);
        SPELLS_BY_SUBCATEGORY.computeIfAbsent(logicInstance.subCategory, k -> new ArrayList<>()).add(itemSupplier);
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

    public static Map<String, List<Supplier<Item>>> getSpellsBySubcategory() {
        return SPELLS_BY_SUBCATEGORY;
    }
} 
