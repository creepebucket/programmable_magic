package org.creepebucket.programmable_magic.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.creepebucket.programmable_magic.entities.SpellEntity;

/**
 * 法术实体渲染器：当前不额外绘制任何粒子/模型，仅维持渲染管线。
 */
public class SpellEntityRenderer extends EntityRenderer<SpellEntity, SpellEntityRenderState> {

    /**
     * 构造：标准上下文转发。
     */
    public SpellEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * 创建渲染状态对象。
     */
    @Override
    public SpellEntityRenderState createRenderState() {
        return new SpellEntityRenderState();
    }

    /**
     * 从实体提取渲染状态（位置/速度等）。
     */
    @Override
    public void extractRenderState(SpellEntity entity, SpellEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.x = entity.getX();
        state.y = entity.getY();
        state.z = entity.getZ();
        var v = entity.getDeltaMovement();
        state.vx = v.x;
        state.vy = v.y;
        state.vz = v.z;
    }

}
