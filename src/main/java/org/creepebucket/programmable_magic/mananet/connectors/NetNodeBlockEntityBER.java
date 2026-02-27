package org.creepebucket.programmable_magic.mananet.connectors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.renderer.api.RenderHelper;
import org.jspecify.annotations.Nullable;

import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

public class NetNodeBlockEntityBER implements BlockEntityRenderer<NetNodeBlockEntity, NetNodeBlockEntityBER.NetNodeBlockEntityBERS> {

    @Override
    public NetNodeBlockEntityBERS createRenderState() {
        return new NetNodeBlockEntityBERS();
    }

    @Override
    public void submit(NetNodeBlockEntityBERS renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        var renderer = new RenderHelper(poseStack, submitNodeCollector, cameraRenderState);
        var pos = new Vec3(0.5, 0.5, 0.5); // haha

        var s = pos.subtract(0, 0, 0.5);
        var n = pos.add     (0, 0, 0.5);
        var e = pos.subtract(0.5, 0, 0);
        var w = pos.add     (0.5, 0, 0);

        renderer.addText(s, Direction.NORTH, 0.01F, -1, FULL_BRIGHT, Component.literal("South").getVisualOrderText());
        renderer.addText(n, Direction.SOUTH, 0.01F, -1, FULL_BRIGHT, Component.literal("North").getVisualOrderText());
        renderer.addText(e, Direction.WEST, 0.01F, -1, FULL_BRIGHT, Component.literal("East").getVisualOrderText());
        renderer.addText(w, Direction.EAST, 0.01F, -1, FULL_BRIGHT, Component.literal("West").getVisualOrderText());

        Vec3 facing = pos;

        switch (renderState.direction) {
            case SOUTH -> facing = s;
            case NORTH -> facing = n;
            case EAST  -> facing = e;
            case WEST  -> facing = w;
        }

        renderer.addText(facing.add(0, 0.2, 0), renderState.direction, 0.01F, -1, FULL_BRIGHT, Component.literal("FACING").getVisualOrderText());
    }

    public static class NetNodeBlockEntityBERS extends BlockEntityRenderState {
        public Direction direction;
    }

    @Override
    public void extractRenderState(NetNodeBlockEntity blockEntity, NetNodeBlockEntityBERS renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        renderState.direction = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getValue(BasicManaConnector.FACING);
    }
}
