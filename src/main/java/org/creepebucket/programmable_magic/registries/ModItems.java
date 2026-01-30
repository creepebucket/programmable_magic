package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 魔杖物品占位符
    public static final DeferredItem<WandItemPlaceholder> WAND_ITEM_PLACEHOLDER = ITEMS.register(
            "wand_item_placeholder", registryName -> new WandItemPlaceholder(new Item.Properties()
                    .stacksTo(64)
                    .component(ModDataComponents.WAND_PLACEHOLDER_ITEM_ID.get(), "minecraft:air")
                    .setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredItem<Wand> WAND = ITEMS.register(
            "rg_alloy_wand", registryName -> new Wand(
                    new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, registryName)),
                    1000, // 法术槽位最大数量（有效容量由插件控制）
                    5     // 插件槽位最大数量
            )
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
