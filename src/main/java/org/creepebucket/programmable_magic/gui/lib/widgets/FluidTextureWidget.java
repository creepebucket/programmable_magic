package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class FluidTextureWidget extends Widget implements Renderable {
	public String fluidId;

	public FluidTextureWidget(Coordinate pos, Coordinate size, String fluidId) {
		super(pos, size);
		this.fluidId = fluidId;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		if (fluidId.isEmpty()) return;

		Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidId));
		FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
		TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
		FluidTintSource tintSource = fluidModel.fluidTintSource();
		int tintColor = tintSource != null ? tintSource.color(fluid.defaultFluidState()) : -1;

		int startX = x();
		int startY = y();
		int endX = x() + w();
		int endY = y() + h();
		for (int yOff = startY; yOff < endY; yOff += 16)
			for (int xOff = startX; xOff < endX; xOff += 16)
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, xOff, yOff, Math.min(16, endX - xOff), Math.min(16, endY - yOff), tintColor);
	}
}
