package org.creepebucket.programmable_magic.gui.machines.generator.pressure_relief_valve;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.utils.ModColors;

public class PressureReliefValveScreen extends MachineScreen<PressureReliefValveMenu> {

	public PressureReliefValveScreen(PressureReliefValveMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();
		root.mainColor(ModColors.MAIN_COLOR_P);
	}
}
