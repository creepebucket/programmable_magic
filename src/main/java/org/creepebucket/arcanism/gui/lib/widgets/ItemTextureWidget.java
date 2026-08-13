package org.creepebucket.arcanism.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.lib.api.Widget;
import org.creepebucket.arcanism.gui.lib.api.widgets.Renderable;

public class ItemTextureWidget extends Widget implements Renderable {
	public String itemId;

	public ItemTextureWidget(Coordinate pos, Coordinate size, String itemId) {
		super(pos, size);
		this.itemId = itemId;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		if (itemId.isEmpty()) return;

		Identifier parsed = Identifier.parse(itemId);
		Identifier id = Identifier.fromNamespaceAndPath(parsed.getNamespace(), "item/" + parsed.getPath());
		SpriteId spriteId = new SpriteId(TextureAtlas.LOCATION_ITEMS, id);
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(spriteId);

		int startX = x();
		int startY = y();
		int endX = x() + w();
		int endY = y() + h();
		for (int yOff = startY; yOff < endY; yOff += 16)
			for (int xOff = startX; xOff < endX; xOff += 16)
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, xOff, yOff, Math.min(16, endX - xOff), Math.min(16, endY - yOff), -1);
	}
}
