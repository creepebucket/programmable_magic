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
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.items.Wand;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<BlockItem> EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER = ITEMS.register(
            "example_universal_multiblock_controller",
            registryName -> new BlockItem(ModBlocks.EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName)))
    );

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

    // 魔杖物品占位符
    public static final DeferredItem<WandItemPlaceholder> WAND_ITEM_PLACEHOLDER = ITEMS.register(
            "wand_item_placeholder", registryName -> new WandItemPlaceholder(new Item.Properties()
                    .stacksTo(64)
                    .component(ModDataComponents.WAND_PLACEHOLDER_ITEM_ID.get(), "minecraft:air")
                    .setId(ResourceKey.create(Registries.ITEM, registryName))))
    ;

    public static final DeferredItem<Wand> WAND = ITEMS.register(
            "rg_alloy_wand", registryName -> new Wand(
                    new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, registryName)),
                    1000, // 法术槽位最大数量（有效容量由插件控制）
                    5     // 插件槽位最大数量
            )
    );

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
