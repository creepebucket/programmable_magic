package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;

import java.util.ArrayList;
import java.util.List;

public abstract class MachineScreen<M extends MachineMenu> extends Screen<M> {
	public List<MachineWidgets.InformationWindowWidget> windows = new ArrayList<>();
	public MachineWidgets.WindowManagementWindow managementWindow;

	public MachineScreen(M menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.blurBeforeThisStratum();
		graphics.fill(0, 0, width, height, 0);
	}

	@Override
	public Widget addWidget(Widget widget) {
		if (widget instanceof MachineWidgets.InformationWindowWidget infoWindow)
			windows.add(infoWindow);
		return super.addWidget(widget);
	}

	@Override
	public void buildWidget() {
		addWidget(new MachineWidgets.WindowHintWidget(Coordinate.fromCenter(0, 0)));
	}

	@Override
	public void init() {
		super.init();
		managementWindow = (MachineWidgets.WindowManagementWindow) addWidget(new MachineWidgets.WindowManagementWindow(Coordinate.fromCenter(-60, -50), Coordinate.fromTopLeft(120, 100)));
		managementWindow.enabled = false;
	}
}
