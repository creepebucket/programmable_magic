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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.creepebucket.programmable_magic.renderer.api.RenderHelper;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

public class NetNodeBlockEntityBER implements BlockEntityRenderer<NetNodeBlockEntity, NetNodeBlockEntityBER.NetNodeBlockEntityBERS> {

    @Override
    public AABB getRenderBoundingBox(NetNodeBlockEntity blockEntity) {
        var selfPos = blockEntity.getBlockPos();
        var boundingBox = new AABB(selfPos);

        for (var connectedPos : blockEntity.getData(ModAttachments.CONNECTIONS).values()) {
            if (selfPos.asLong() > connectedPos.asLong()) continue;
            boundingBox = boundingBox.minmax(new AABB(connectedPos));
        }

        return boundingBox;
    }

    @Override
    public NetNodeBlockEntityBERS createRenderState() {
        return new NetNodeBlockEntityBERS();
    }

    @Override
    public void submit(NetNodeBlockEntityBERS renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        var renderer = new RenderHelper(poseStack, submitNodeCollector, cameraRenderState);
        var position = new Vector3f(0.5F, 0.5F, 0.5F);

        var n = new Vector3f(0, 0, -0.375F);
        var s = new Vector3f(0, 0, 0.375F);
        var w = new Vector3f(-0.375F, 0, 0);
        var e = new Vector3f(0.375F, 0, 0);

        renderer.addText(new Vector3f(position).add(s), Direction.NORTH, 0.01F, -1, FULL_BRIGHT, Component.literal("South").getVisualOrderText());
        renderer.addText(new Vector3f(position).add(n), Direction.SOUTH, 0.01F, -1, FULL_BRIGHT, Component.literal("North").getVisualOrderText());
        renderer.addText(new Vector3f(position).add(e), Direction.WEST, 0.01F, -1, FULL_BRIGHT, Component.literal("East").getVisualOrderText());
        renderer.addText(new Vector3f(position).add(w), Direction.EAST, 0.01F, -1, FULL_BRIGHT, Component.literal("West").getVisualOrderText());

        var facingToPos = Map.of(Direction.SOUTH, s, Direction.NORTH, n, Direction.EAST, e, Direction.WEST, w);
        var facingToXPixel = Map.of(Direction.SOUTH, new Vector3f(-1.0F / 16, 0, 0), Direction.NORTH, new Vector3f(1.0F / 16, 0, 0),
                Direction.WEST, new Vector3f(0, 0, -1.0F / 16), Direction.EAST, new Vector3f(0, 0, 1.0F / 16));
        var facingToCpDirection = Map.of(Direction.SOUTH, new Vector3f(0, 0, 0.5F), Direction.NORTH, new Vector3f(0, 0, 0.5F),
                Direction.WEST, new Vector3f(0.5F, 0, 0), Direction.EAST, new Vector3f(0.5F, 0, 0));

        var yPixel = new Vector3f(0, 1.0F / 16, 0);

        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal() || !renderState.connections.containsKey(direction)) continue;
            var connectedBlockPos = renderState.connections.get(direction);

            if (renderState.blockPos.asLong() > connectedBlockPos.asLong()) continue;

            var connectedFace = renderState.connectedFaces.get(direction);
            if (connectedFace == null) {
                continue;
            }

            var selfPos = new Vector3f(position).add(facingToPos.get(direction));
            var xPixel = new Vector3f(facingToXPixel.get(direction));

            var selfCenters = List.of(
                    new Vector3f(selfPos).add(new Vector3f(yPixel).mul(-3.5F)).add(new Vector3f(xPixel).mul(1.5F)),
                    new Vector3f(selfPos).add(new Vector3f(yPixel).mul(-3.5F)).add(new Vector3f(xPixel).mul(-1.5F)),
                    new Vector3f(selfPos).add(new Vector3f(yPixel).mul(-2.1F)).add(new Vector3f(xPixel).mul(3.9F)),
                    new Vector3f(selfPos).add(new Vector3f(yPixel).mul(-2.1F)).add(new Vector3f(xPixel).mul(-3.9F))
            );

            var connectedPos = connectedBlockPos.getCenter().subtract(renderState.blockPos.getCenter()).toVector3f().add(facingToPos.get(connectedFace)).add(position);
            xPixel = new Vector3f(facingToXPixel.get(connectedFace)).mul(-1);

            var connectedCenters = List.of(
                    new Vector3f(connectedPos).add(new Vector3f(yPixel).mul(-3.5F)).add(new Vector3f(xPixel).mul(1.5F)),
                    new Vector3f(connectedPos).add(new Vector3f(yPixel).mul(-3.5F)).add(new Vector3f(xPixel).mul(-1.5F)),
                    new Vector3f(connectedPos).add(new Vector3f(yPixel).mul(-2.1F)).add(new Vector3f(xPixel).mul(3.9F)),
                    new Vector3f(connectedPos).add(new Vector3f(yPixel).mul(-2.1F)).add(new Vector3f(xPixel).mul(-3.9F))
            );

            List<List<Vector3f>> allCenters = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                var start = selfCenters.get(i);
                var end = connectedCenters.get(i);
                var cp0 = new Vector3f(start).add((end.x - start.x) * facingToCpDirection.get(direction).x, 0, (end.z - start.z) * facingToCpDirection.get(direction).z);
                var cp1 = new Vector3f(end).add((start.x - end.x) * facingToCpDirection.get(connectedFace).x, 0, (start.z - end.z) * facingToCpDirection.get(connectedFace).z);

                allCenters.add(ModUtils.BezierUtils.generateCubicCurve(start, cp0, cp1, end, 10));
            }

            List<List<List<Vector3f>>> allVertex = new ArrayList<>(List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

            for (int i = 0; i < 4; i++) {
                var centers = allCenters.get(i);
                Vector3f lastNormalXPixel = null;

                for (int j = 0; j < centers.size(); j++) {
                    Vector3f avg;
                    if (j == 0) avg = facingToCpDirection.get(direction);
                    else if (j == centers.size() - 1) avg = facingToCpDirection.get(connectedFace);
                    else {
                        var leftDir = new Vector3f(centers.get(j)).sub(centers.get(j - 1));
                        var rightDir = new Vector3f(centers.get(j + 1)).sub(centers.get(j));
                        avg = leftDir.mul(0.5F).add(rightDir.mul(0.5F));
                    }

                    var normalXPixel = avg.cross(avg.y > 0 ? new Vector3f(0, -1, 0) : new Vector3f(0, 1, 0), new Vector3f()).normalize(1F / 32);
                    var normalYPixel = avg.cross(normalXPixel, new Vector3f()).normalize(1F / 32);
                    if (lastNormalXPixel != null && lastNormalXPixel.dot(normalXPixel) < 0) {
                        normalXPixel.mul(-1);
                        normalYPixel.mul(-1);
                    }
                    lastNormalXPixel = normalXPixel;

                    var center = centers.get(j);
                    allVertex.get(i).add(List.of(
                            new Vector3f(center).add(-normalXPixel.x - normalYPixel.x, -normalXPixel.y - normalYPixel.y, -normalXPixel.z - normalYPixel.z),
                            new Vector3f(center).add(-normalXPixel.x + normalYPixel.x, -normalXPixel.y + normalYPixel.y, -normalXPixel.z + normalYPixel.z),
                            new Vector3f(center).add( normalXPixel.x + normalYPixel.x,  normalXPixel.y + normalYPixel.y,  normalXPixel.z + normalYPixel.z),
                            new Vector3f(center).add( normalXPixel.x - normalYPixel.x,  normalXPixel.y - normalYPixel.y,  normalXPixel.z - normalYPixel.z)
                    ));
                }
            }

            var colors = List.of(0xFFFF0000, 0xFFFFFF00, 0xFF0000FF, 0xFF00FF00);

            for (int i = 0; i < 4; i++) {
                var vertexList = allVertex.get(i);

                for (int j = 0; j < vertexList.size() - 1; j++) {

                    renderer.addSolidQuad(vertexList.get(j).get(0), vertexList.get(j).get(1), vertexList.get(j + 1).get(1), vertexList.get(j + 1).get(0), colors.get(i), renderState.lightCoords);
                    renderer.addSolidQuad(vertexList.get(j).get(1), vertexList.get(j).get(2), vertexList.get(j + 1).get(2), vertexList.get(j + 1).get(1), colors.get(i), renderState.lightCoords);
                    renderer.addSolidQuad(vertexList.get(j).get(2), vertexList.get(j).get(3), vertexList.get(j + 1).get(3), vertexList.get(j + 1).get(2), colors.get(i), renderState.lightCoords);
                    renderer.addSolidQuad(vertexList.get(j).get(3), vertexList.get(j).get(0), vertexList.get(j + 1).get(0), vertexList.get(j + 1).get(3), colors.get(i), renderState.lightCoords);
                }
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
