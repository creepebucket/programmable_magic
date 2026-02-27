package org.creepebucket.programmable_magic.mananet.connectors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.renderer.api.ModVec3;
import org.creepebucket.programmable_magic.renderer.api.RenderHelper;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

public class NetNodeBlockEntityBER implements BlockEntityRenderer<NetNodeBlockEntity, NetNodeBlockEntityBER.NetNodeBlockEntityBERS> {

    @Override
    public NetNodeBlockEntityBERS createRenderState() {
        return new NetNodeBlockEntityBERS();
    }

    @Override
    public void submit(NetNodeBlockEntityBERS renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        var renderer = new RenderHelper(poseStack, submitNodeCollector, cameraRenderState);
        var position = new Vec3(0.5, 0.5, 0.5); // haha

        var n = position.subtract(0, 0, 0.375);
        var s = position.add     (0, 0, 0.375);
        var w = position.subtract(0.375, 0, 0);
        var e = position.add     (0.375, 0, 0);

        renderer.addText(s, Direction.NORTH, 0.01F, -1, FULL_BRIGHT, Component.literal("South").getVisualOrderText());
        renderer.addText(n, Direction.SOUTH, 0.01F, -1, FULL_BRIGHT, Component.literal("North").getVisualOrderText());
        renderer.addText(e, Direction.WEST, 0.01F, -1, FULL_BRIGHT, Component.literal("East").getVisualOrderText());
        renderer.addText(w, Direction.EAST, 0.01F, -1, FULL_BRIGHT, Component.literal("West").getVisualOrderText());

        var facingToPos = Map.of(Direction.SOUTH, s, Direction.NORTH, n, Direction.EAST, e, Direction.WEST, w);
        var facingToXPixel = Map.of(Direction.SOUTH, new ModVec3(-1.0 / 16, 0, 0), Direction.NORTH, new ModVec3(1.0 / 16, 0, 0),
                                    Direction.WEST , new ModVec3(0, 0, -1.0 / 16), Direction.EAST , new ModVec3(0, 0, 1.0 / 16));
        var yPixel = new ModVec3(0, 1.0/16, 0);
        var connectableDirections = List.of(renderState.direction.getClockWise(), renderState.direction.getCounterClockWise());
        var connectablePoses = List.of(facingToPos.get(renderState.direction.getClockWise()), facingToPos.get(renderState.direction.getCounterClockWise()));

        renderer.addText(facingToPos.get(renderState.direction).add(0, 0.2, 0), renderState.direction, 0.01F, -1, FULL_BRIGHT, Component.literal("FACING").getVisualOrderText());
        renderer.addText(connectablePoses.get(0).add(0, 0.2, 0), connectableDirections.get(0), 0.01F, -1, FULL_BRIGHT, Component.literal("CONNECTABLE").getVisualOrderText());
        renderer.addText(connectablePoses.get(1).add(0, 0.2, 0), connectableDirections.get(1), 0.01F, -1, FULL_BRIGHT, Component.literal("CONNECTABLE").getVisualOrderText());

        for (Direction direction : connectableDirections) {
            var pos = facingToPos.get(direction);
            var xPixel = facingToXPixel.get(direction);

            renderer.addAxisAlienedSolidQuad(pos.subtract(yPixel.multiply(3)).add(xPixel.multiply(1)), xPixel.subtract(yPixel), -1, FULL_BRIGHT);
            renderer.addAxisAlienedSolidQuad(pos.subtract(yPixel.multiply(3)).subtract(xPixel.multiply(1)), xPixel.multiply(-1).subtract(yPixel), -1, FULL_BRIGHT);
            renderer.addAxisAlienedSolidQuad(pos.subtract(yPixel.multiply(1.6)).add(xPixel.multiply(3.4)), xPixel.subtract(yPixel), -1, FULL_BRIGHT);
            renderer.addAxisAlienedSolidQuad(pos.subtract(yPixel.multiply(1.6)).subtract(xPixel.multiply(3.4)), xPixel.multiply(-1).subtract(yPixel), -1, FULL_BRIGHT);

        }
    }

    public static class NetNodeBlockEntityBERS extends BlockEntityRenderState {
        public Direction direction;
        public Map<Direction, BlockPos> connections;

    }

    @Override
    public void extractRenderState(NetNodeBlockEntity blockEntity, NetNodeBlockEntityBERS renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        renderState.direction = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getValue(BasicManaConnector.FACING);
    }
}
