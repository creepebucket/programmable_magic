package org.creepebucket.programmable_magic.gui.wand;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ReleaseSpellButton extends Button{
    private static final ResourceLocation TEXTURE_NORMAL = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/release_spell_button_normal.png");
    private static final ResourceLocation TEXTURE_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/release_spell_button_hover.png");

    protected ReleaseSpellButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHoveredOrFocused();
        ResourceLocation texture = hovered ? TEXTURE_HOVER : TEXTURE_NORMAL;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getX() - 79, this.getY(), 1.0f, 1.0f, this.width + 79 * 2, this.height, this.width + 79 * 2, this.height);
        //guiGraphics.blit(texture,this.getX() - 79,this.getY(),this.getX() + this.width + 79,this.getY() + this.height,0.0F,1.0F,0.0F,1.0F);
    }
}
