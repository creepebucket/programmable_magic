package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;

public abstract class MachineScreen<M extends MachineMenu> extends Screen<M> {

	public MachineScreen(M menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.blurBeforeThisStratum();
	}
}
