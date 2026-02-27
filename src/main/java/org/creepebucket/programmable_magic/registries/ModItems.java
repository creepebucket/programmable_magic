package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.spells.PackedSpell;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // =====普通物品=====
    public static final DeferredItem<WandItemPlaceholder> WAND_ITEM_PLACEHOLDER =
            ITEMS.register("wand_item_placeholder", registryName -> new WandItemPlaceholder(  stackTo(64, registryName)));

    public static final DeferredItem<Wand> WAND =
            ITEMS.register("rg_alloy_wand",         registryName -> new Wand(                 stackTo(1, registryName), 1000, 5));

    public static final DeferredItem<PackedSpell> PACKED_SPELL =
            ITEMS.register("packed_spell",          registryName -> new PackedSpell(          stackTo(64, registryName)));

    // =====方块物品=====
    public static final DeferredItem<BlockItem> BASIC_MAMA_CONNECTOR_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.BASIC_MANA_CONNECTOR);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    // =====工具方法=====
    public static Item.Properties stackTo(int to, Identifier registryName) {
        return new Item.Properties().stacksTo(to).setId(ResourceKey.create(Registries.ITEM, registryName));
    }
}
