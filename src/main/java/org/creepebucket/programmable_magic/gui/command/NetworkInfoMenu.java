package org.creepebucket.programmable_magic.gui.command;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.mananet.NetworkManaManager;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

import java.util.Map;

public class NetworkInfoMenu extends Menu {
	public SyncedValue<Double> updateInterval;
	public SyncedValue<Map<Long, Map<String, ModUtils.Mana>>> datas;
	public int count = 0;

	public NetworkInfoMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv);
	}

	public NetworkInfoMenu(int containerId, Inventory playerInv) {
		super(ModMenuTypes.NETWORK_INFO.get(), containerId, playerInv, InteractionHand.MAIN_HAND, Menu::init);
	}

	@Override
	public void init() {
		updateInterval = registerData("update_interval", SyncMode.BOTH, 15d);
		datas = registerData("mana_data", SyncMode.BOTH, NetworkManaManager.getAllData(playerInv.player.level()));
	}

	@Override
	public void tick() {
		count++;

		if (count >= updateInterval.get()) {
			count = 0;

			// 网络更新逻辑
			datas.set(NetworkManaManager.getAllData(playerInv.player.level()));
		}
	}
}
