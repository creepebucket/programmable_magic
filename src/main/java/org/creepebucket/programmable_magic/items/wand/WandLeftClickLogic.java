package org.creepebucket.programmable_magic.items.wand;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModItemTagProvider;
import org.creepebucket.programmable_magic.registries.ModTagKeys;

import java.util.List;
import java.util.stream.Collectors;

public class WandLeftClickLogic {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        logic(event);
    }

    public static void logic(PlayerInteractEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();

        if (stack.is(ModTagKeys.WAND) && level.isClientSide) {
            //发射 SpellStorageMenu存储的法术

            List<ItemStack> spells = stack.getOrDefault(ModDataComponents.WAND_SPELLS_STORAGE, List.of());
            System.out.println(spells.size());

            List<ItemStack> filtered = spells.stream()
                    .filter(s -> s != null && !s.isEmpty() && s.getCount() > 0)
                    .map(s -> {
                        ItemStack c = s.copy();
                        int clamped = Math.min(Math.max(c.getCount(), 1), 99);
                        c.setCount(clamped);
                        return c;
                    })
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                ClientPacketDistributor.sendToServer(new SpellReleasePacket(filtered));
            }
        }
    }
}
