package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import org.creepebucket.programmable_magic.items.mana_cell.SmallManaCell;
import org.creepebucket.programmable_magic.items.mana_cell.MediumManaCell;
import org.creepebucket.programmable_magic.items.mana_cell.LargeManaCell;
import org.creepebucket.programmable_magic.items.mana_cell.HugeManaCell;
import org.creepebucket.programmable_magic.items.mana_cell.ColossalManaCell;
import org.creepebucket.programmable_magic.items.wand.TestWand;
import org.creepebucket.programmable_magic.items.wand.BaseWand;
import net.minecraft.world.item.Item;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<SmallManaCell> SMALL_MANA_CELL_DEFERRED_ITEM = ITEMS.register(
            "small_mana_cell", registryName -> new SmallManaCell(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

    public static final DeferredItem<MediumManaCell> MEDIUM_MANA_CELL = ITEMS.register(
            "medium_mana_cell", registryName -> new MediumManaCell(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

    public static final DeferredItem<LargeManaCell> LARGE_MANA_CELL = ITEMS.register(
            "large_mana_cell", registryName -> new LargeManaCell(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

    public static final DeferredItem<HugeManaCell> HUGE_MANA_CELL = ITEMS.register(
            "huge_mana_cell", registryName -> new HugeManaCell(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

    public static final DeferredItem<ColossalManaCell> COLOSSAL_MANA_CELL = ITEMS.register(
            "colossal_mana_cell", registryName -> new ColossalManaCell(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

    public static final DeferredItem<TestWand> TEST_WAND_DEFERRED_ITEM = ITEMS.register(
            "test_wand", registryName -> new TestWand(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

    // 线缆方块物品，便于测试与发布
    public static final DeferredItem<BlockItem> MANA_CABLE_ITEM = ITEMS.register(
            "mana_cable", registryName -> new BlockItem(
                    ModBlocks.MANA_CABLE.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
            )
    );

    // 通用材料与工具
    public static final DeferredItem<Item> PURE_REDSTONE_DUST = ITEMS.register(
            "pure_redstone_dust", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> REDSTONE_GOLD_ALLOY = ITEMS.register(
            "redstone_gold_alloy", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> RG_ALLOY_WIRE = ITEMS.register(
            "rg_alloy_wire", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> RG_ALLOY_ROD = ITEMS.register(
            "rg_alloy_rod", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> SMALL_CELL_SHELL = ITEMS.register(
            "small_cell_shell", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;

    public static final DeferredItem<Item> DEBRIS_DUST = ITEMS.register(
            "debris_dust", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> DEBRIS_CLAY = ITEMS.register(
            "debris_clay", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> COVERED_RG_ALLOY_WIRE = ITEMS.register(
            "covered_rg_alloy_wire", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;
    public static final DeferredItem<Item> SMALL_CELL_CERTIDGE = ITEMS.register(
            "small_cell_certidge", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;

    public static final DeferredItem<BaseWand> RG_ALLOY_WAND = ITEMS.register(
            "rg_alloy_wand", registryName -> new BaseWand(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, registryName)), 1.00, 20))
    ;

    // 方块物品
    public static final DeferredItem<BlockItem> PRIMITIVE_ALLOY_SMELTER_ITEM = ITEMS.register(
            "primitive_alloy_smelter", registryName -> new BlockItem(
                    ModBlocks.PRIMITIVE_ALLOY_SMELTER.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
            )
    );

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
