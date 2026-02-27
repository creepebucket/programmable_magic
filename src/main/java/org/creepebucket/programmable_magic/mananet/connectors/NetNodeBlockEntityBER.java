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
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.creepebucket.programmable_magic.renderer.api.ModVec3;
import org.creepebucket.programmable_magic.renderer.api.RenderHelper;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
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
        var position = new Vec3(0.5, 0.5, 0.5);

        var n = new Vec3(0, 0, -0.375);
        var s = new Vec3(0, 0,  0.375);
        var w = new Vec3(-0.375, 0, 0);
        var e = new Vec3( 0.375, 0, 0);

        renderer.addText(position.add(s), Direction.NORTH, 0.01F, -1, FULL_BRIGHT, Component.literal("South").getVisualOrderText());
        renderer.addText(position.add(n), Direction.SOUTH, 0.01F, -1, FULL_BRIGHT, Component.literal("North").getVisualOrderText());
        renderer.addText(position.add(e), Direction.WEST , 0.01F, -1, FULL_BRIGHT, Component.literal("East") .getVisualOrderText());
        renderer.addText(position.add(w), Direction.EAST , 0.01F, -1, FULL_BRIGHT, Component.literal("West") .getVisualOrderText());

        var facingToPos = Map.of(Direction.SOUTH, s, Direction.NORTH, n, Direction.EAST, e, Direction.WEST, w);
        var facingToXPixel = Map.of(Direction.SOUTH, new ModVec3(-1.0 / 16, 0, 0), Direction.NORTH, new ModVec3(1.0 / 16, 0, 0),
                                    Direction.WEST, new ModVec3(0, 0, -1.0 / 16), Direction.EAST, new ModVec3(0, 0, 1.0 / 16));
        var yPixel = new ModVec3(0, 1.0 / 16, 0);
        var connectableDirections = List.of(renderState.direction.getClockWise(), renderState.direction.getCounterClockWise());

        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal() || !renderState.connections.containsKey(direction)) continue;
            var connectedBlockPos = renderState.connections.get(direction);
            var connectedFace = renderState.connectedFaces.get(direction);

            if (connectedBlockPos == null || connectedFace == null) {
                return;
            }

            var selfPos = position.add(facingToPos.get(direction));
            var xPixel = facingToXPixel.get(direction);

            var selfPoints = List.of(
                             // p0
                             List.of(selfPos.add(yPixel.multiply(-3)).add(xPixel.multiply(1)), selfPos.add(yPixel.multiply(-3)).add(xPixel.multiply(2)),
                                     selfPos.add(yPixel.multiply(-4)).add(xPixel.multiply(2)), selfPos.add(yPixel.multiply(-4)).add(xPixel.multiply(1))),
                             // p1
                             List.of(selfPos.add(yPixel.multiply(-3)).add(xPixel.multiply(-1)), selfPos.add(yPixel.multiply(-3)).add(xPixel.multiply(-2)),
                                     selfPos.add(yPixel.multiply(-4)).add(xPixel.multiply(-2)), selfPos.add(yPixel.multiply(-4)).add(xPixel.multiply(-1))),
                             // p2
                             List.of(selfPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(3.4)), selfPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(4.4)),
                                     selfPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(4.4)), selfPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(3.4))),
                             // p3
                             List.of(selfPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(-3.4)), selfPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(-4.4)),
                                     selfPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(-4.4)), selfPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(-3.4)))
                             );

            var connectedPos = connectedBlockPos.getCenter().subtract(renderState.blockPos.getCenter()).add(facingToPos.get(connectedFace)).add(position);
            xPixel = facingToXPixel.get(connectedFace).multiply(-1);
            var connectedPoints = List.of(
                             // p0
                             List.of(connectedPos.add(yPixel.multiply(-3)).add(xPixel.multiply(1)), connectedPos.add(yPixel.multiply(-3)).add(xPixel.multiply(2)),
                                     connectedPos.add(yPixel.multiply(-4)).add(xPixel.multiply(2)), connectedPos.add(yPixel.multiply(-4)).add(xPixel.multiply(1))),
                             // p1
                             List.of(connectedPos.add(yPixel.multiply(-3)).add(xPixel.multiply(-1)), connectedPos.add(yPixel.multiply(-3)).add(xPixel.multiply(-2)),
                                     connectedPos.add(yPixel.multiply(-4)).add(xPixel.multiply(-2)), connectedPos.add(yPixel.multiply(-4)).add(xPixel.multiply(-1))),
                             // p2
                             List.of(connectedPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(3.4)), connectedPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(4.4)),
                                     connectedPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(4.4)), connectedPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(3.4))),
                             // p3
                             List.of(connectedPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(-3.4)), connectedPos.add(yPixel.multiply(-1.6)).add(xPixel.multiply(-4.4)),
                                     connectedPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(-4.4)), connectedPos.add(yPixel.multiply(-2.6)).add(xPixel.multiply(-3.4)))
                             );

            for (int i = 0; i < 4; i++) {
                var self = selfPoints.get(i);
                var connected = connectedPoints.get(i);

                renderer.addSolidQuad(self.get(0), self.get(1), connected.get(1), connected.get(0), -1, renderState.lightCoords);
                renderer.addSolidQuad(self.get(1), self.get(2), connected.get(2), connected.get(1), 0xFFFF0000, renderState.lightCoords);
                renderer.addSolidQuad(self.get(2), self.get(3), connected.get(3), connected.get(2), 0xFF00FF00, renderState.lightCoords);
                renderer.addSolidQuad(self.get(3), self.get(0), connected.get(0), connected.get(3), 0xFF0000FF, renderState.lightCoords);
            }
        }
    }

    public static class NetNodeBlockEntityBERS extends BlockEntityRenderState {
        public Direction direction;
        public Map<Direction, BlockPos> connections;
        public Map<Direction, Direction> connectedFaces;
        public BlockPos pos;

    }

    @Override
    public void extractRenderState(NetNodeBlockEntity blockEntity, NetNodeBlockEntityBERS renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        renderState.direction = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getValue(BasicManaConnector.FACING);
        renderState.connections = blockEntity.getData(ModAttachments.CONNECTIONS);
        renderState.connectedFaces = new HashMap<>();
        renderState.pos = blockEntity.getBlockPos();

        for (var entry : renderState.connections.entrySet()) {
            var connectedPos = entry.getValue();
            var connected = blockEntity.getLevel().getBlockEntity(connectedPos);
            if (connected == null) continue;

            for (var backEntry : connected.getData(ModAttachments.CONNECTIONS).entrySet()) {
                if (!backEntry.getValue().equals(renderState.pos)) continue;
                renderState.connectedFaces.put(entry.getKey(), backEntry.getKey());
                break;
            }
        }
    }
}
