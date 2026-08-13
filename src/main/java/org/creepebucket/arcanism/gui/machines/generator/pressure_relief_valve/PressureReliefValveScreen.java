package org.creepebucket.arcanism.gui.machines.generator.pressure_relief_valve;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.machines.api.MachineScreen;
import org.creepebucket.arcanism.gui.machines.api.MachineWidgets;
import org.creepebucket.arcanism.utils.ModColors;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;

public class PressureReliefValveScreen extends MachineScreen<PressureReliefValveMenu> {

	public PressureReliefValveScreen(PressureReliefValveMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_P);

		// ================= 顶栏 ================= //

		addWidget(new MachineWidgets.MachineInfoWindow(fromCenter(-175, -100), fromTopLeft(350, 70), menu.manaPowerW, Component.translatable("gui.arcanism.machine.mana_type.pressure"),
				Component.translatable("gui.arcanism.machine.type.pressure_relief_valve"), Component.translatable("gui.arcanism.machine.main_text.current_power"), literal("P="), "W"));

		// ================= 网络信息窗口 ================= //

		addWidget(new MachineWidgets.NetworkInfoWindow(fromCenter(-175, -20), fromTopLeft(170, 100), menu));

		// ================= 超频窗口 ================= //

		addWidget(new MachineWidgets.GeneratorOverclockWindow(fromCenter(5, -20), fromTopLeft(170, 50), menu.powerFact, 1e6, 5));

		// ================= 控制窗口 ================= //

		addWidget(new MachineWidgets.MachineControlWindow(fromCenter(5, 40), fromTopLeft(170, 40), menu));
	}
}
