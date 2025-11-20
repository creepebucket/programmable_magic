package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import org.creepebucket.programmable_magic.items.mana_cell.SmallManaCell;
import org.creepebucket.programmable_magic.items.wand.TestWand;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<SmallManaCell> SMALL_MANA_CELL_DEFERRED_ITEM = ITEMS.register(
            "small_mana_cell", registryName -> new SmallManaCell(new Item.Properties()
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

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
