package org.creepebucket.programmable_magic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.client.renderer.SpellEntityRenderState;

public class SpellEntityRenderer extends EntityRenderer<SpellEntity, SpellEntityRenderState> {
    
    public SpellEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public SpellEntityRenderState createRenderState() {
        return new SpellEntityRenderState();
    }
    
    @Override
    public void extractRenderState(SpellEntity entity, SpellEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }
    
    @Override
    public void render(SpellEntityRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(state, poseStack, bufferSource, packedLight);
    }
} 