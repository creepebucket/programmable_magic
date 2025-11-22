package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID + ".main"))
            .icon(() -> new ItemStack(ModItems.RG_ALLOY_WAND.get()))
            .displayItems((params, output) -> {
                // 将本模组命名空间下的所有物品放入，但排除法术展示物品（spell_display_*）
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> {
                            try {
                                var key = BuiltInRegistries.ITEM.getKey(item);
                                if (!key.getNamespace().equals(MODID)) return false;
                                return !key.getPath().startsWith("spell_display_");
                            } catch (Exception e) { return false; }
                        })
                        .forEach(output::accept);
            })
            .build());

    public static void register(IEventBus bus) { CREATIVE_MODE_TABS.register(bus); }
}
