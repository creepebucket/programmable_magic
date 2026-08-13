package org.creepebucket.arcanism.events.balance_changes;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import org.creepebucket.arcanism.ModConfig;

import static org.creepebucket.arcanism.Arcanism.MODID;

@EventBusSubscriber(modid = MODID)
public class BalanceFuel {

	@SubscribeEvent
	public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
		if (!ModConfig.CONFIG.moreBalancedFuel.get()) return;

		if (event.getItemStack().is(ItemTags.LOGS)) {
			event.setBurnTime(1200);
			return;
		}

		Item item = event.getItemStack().getItem();
		switch (item) {
			case Item i when i == Items.CHARCOAL -> event.setBurnTime(450);
			case Item i when i == Items.COAL -> event.setBurnTime(375);
			case Item i when i == Items.COAL_BLOCK -> event.setBurnTime(3375);
			case Item i when i == Items.LAVA_BUCKET -> event.setBurnTime(1500);
			case Item i when i == Items.BLAZE_ROD -> event.setBurnTime(4500);
			case Item i when i == Items.BLAZE_POWDER -> event.setBurnTime(2250);
			default -> {}
		}
	}
}
