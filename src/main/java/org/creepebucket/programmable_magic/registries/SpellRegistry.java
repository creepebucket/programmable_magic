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
import org.creepebucket.programmable_magic.spells.base_spell.VelocitySpell;
import org.creepebucket.programmable_magic.spells.base_spell.ApplyPotionSpell;
import org.creepebucket.programmable_magic.spells.base_spell.ProjectileAttachSpell;
import org.creepebucket.programmable_magic.spells.base_spell.PlaceBlockSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.*;
import org.creepebucket.programmable_magic.spells.compute_mod.EntityVelocitySpell;
import org.creepebucket.programmable_magic.spells.control_mod.ConditionInverter;
import org.creepebucket.programmable_magic.spells.control_mod.DelaySpell;
import org.creepebucket.programmable_magic.spells.control_mod.TriggerSpell;

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
        registerSpell(() -> new ValueLiteralSpell(SpellValueType.VECTOR3, "compute_unit_y", new Vec3(0.0, 1.0, 0.0),
                List.of(Component.translatable("item.programmable_magic.spell_display_compute_unit_y"))));
        registerSpell(() -> new ValueLiteralSpell(SpellValueType.VECTOR3, "compute_unit_z", new Vec3(0.0, 0.0, 1.0),
                List.of(Component.translatable("item.programmable_magic.spell_display_compute_unit_z"))));

        registerSpell(DynamicConstantSpell.ViewVectorSpell::new);
        registerSpell(DynamicConstantSpell.CasterEntitySpell::new);
        registerSpell(DynamicConstantSpell.CasterPosSpell::new);
        registerSpell(DynamicConstantSpell.SpellPosSpell::new);
        registerSpell(DynamicConstantSpell.SpellEntitySpell::new);
        registerSpell(DynamicConstantSpell.NearestEntitySpell::new);

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

        // COMPUTE_MOD: 一般运算
        registerSpell(EntityVelocitySpell::new);

        // 基础法术
        registerSpell(ExplosionSpell::new);
        registerSpell(VelocitySpell::new);
        registerSpell(ApplyPotionSpell::new);
        registerSpell(ProjectileAttachSpell::new);
        registerSpell(PlaceBlockSpell::new);

        // CONTROL_MOD
        registerSpell(DelaySpell::new);
        registerSpell(ConditionInverter::new);

        // CONTROL_MOD: 触发器
        registerSpell(TriggerSpell.TriggerTouchGround::new);
        registerSpell(TriggerSpell.TriggerTouchEntity::new);

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
