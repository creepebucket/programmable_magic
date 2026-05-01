package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class WindTurbineMenu extends Menu {

    public SyncedValue<Double> number;

    public WindTurbineMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        this(containerId, playerInv);
    }

    public WindTurbineMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, InteractionHand.MAIN_HAND);
    }

    public WindTurbineMenu(int containerId, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.MACHINE_MENU.get(), containerId, playerInv, hand, Menu::init);
    }

    protected WindTurbineMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
        super(type, containerId, playerInv, hand, definition);
    }

    @Override
    public void init() {
        number = registerData("number", SyncMode.LOCAL_ONLY, 1d);
    }

    public int count;
    @Override
    public void tick() {
    }
}
