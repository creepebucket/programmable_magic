package org.creepebucket.programmable_magic.mananet.connectors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.creepebucket.programmable_magic.renderer.api.RenderHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        var position = new Vec3(0.5, 0.5, 0.5);

        var facingToPos = Map.of(Direction.SOUTH, new Vec3(0, 0, 0.375), Direction.NORTH, new Vec3(0, 0, -0.375),
                Direction.EAST, new Vec3(0.375, 0, 0), Direction.WEST, new Vec3(-0.375, 0, 0));
        var facingToUnit = Map.of(Direction.SOUTH, new Vec3(0, 0, 1), Direction.NORTH, new Vec3(0, 0, -1),
                Direction.EAST, new Vec3(1, 0, 0), Direction.WEST, new Vec3(-1, 0, 0));
        var facingToCpDirection = Map.of(Direction.SOUTH, new Vec3(0, 0, 0.5), Direction.NORTH, new Vec3(0, 0, 0.5),
                Direction.WEST, new Vec3(0.5, 0, 0), Direction.EAST, new Vec3(0.5, 0, 0));

        var offsets = List.of(3.9 / 16, 1.5 / 16, -1.5 / 16, -3.9 / 16);
        var yOffsets = List.of(-2.1, -3.5, -3.5, -2.1);
        var colors = List.of(0xFF00ADBC, 0xFF904242, 0xFFBCB000, 0xFF9FC400, 0xff0099a7, 0xff6f2e2e, 0xffa79c00, 0xff73a100);
        var yPixel = new Vec3(0, 1.0 / 16, 0);

        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal() || !renderState.connections.containsKey(direction)) continue;
            var connectedBlockPos = renderState.connections.get(direction);
            if (renderState.blockPos.asLong() > connectedBlockPos.asLong()) continue;

            var connectedFace = renderState.connectedFaces.get(direction);
            if (connectedFace == null) continue;
            var connectedBlockFacing = renderState.connectedDirections.get(direction);
            if (connectedBlockFacing == null) continue;

            var selfBlockFacing = renderState.direction;
            var selfBase = position.add(facingToPos.get(direction));
            var connectedBase = connectedBlockPos.getCenter().subtract(renderState.blockPos.getCenter()).add(position).add(facingToPos.get(connectedFace));

            List<List<Vec3>> allCenters = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                var selfCenter = selfBase.add(facingToUnit.get(selfBlockFacing).scale(offsets.get(i))).add(yPixel.scale(yOffsets.get(i)));
                var connectedCenter = connectedBase.add(facingToUnit.get(connectedBlockFacing).scale(offsets.get(i))).add(yPixel.scale(yOffsets.get(i)));
                var cp0 = selfCenter.add((connectedCenter.x - selfCenter.x) * facingToCpDirection.get(direction).x,
                        0, (connectedCenter.z - selfCenter.z) * facingToCpDirection.get(direction).z);
                var cp1 = connectedCenter.add((selfCenter.x - connectedCenter.x) * facingToCpDirection.get(connectedFace).x,
                        0, (selfCenter.z - connectedCenter.z) * facingToCpDirection.get(connectedFace).z);
                allCenters.add(ModUtils.BezierUtils.generateCubicCurve(selfCenter, cp0, cp1, connectedCenter, 10));
            }

            List<List<List<Vec3>>> allVertex = new ArrayList<>(List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
            for (int i = 0; i < 4; i++) {
                var centers = allCenters.get(i);
                Vec3 lastNormalXPixel = null;
                for (int j = 0; j < centers.size(); j++) {
                    Vec3 avg;
                    if (j == 0) avg = facingToCpDirection.get(direction);
                    else if (j == centers.size() - 1) avg = facingToCpDirection.get(connectedFace);
                    else {
                        var leftDir = centers.get(j).subtract(centers.get(j - 1));
                        var rightDir = centers.get(j + 1).subtract(centers.get(j));
                        avg = leftDir.scale(0.5).add(rightDir.scale(0.5));
                    }
                    var normalXPixel = avg.cross(avg.y > 0 ? new Vec3(0, -1, 0) : new Vec3(0, 1, 0)).normalize().scale(1.0 / 32);
                    var normalYPixel = avg.cross(normalXPixel).normalize().scale(1.0 / 32);
                    if (lastNormalXPixel != null && lastNormalXPixel.dot(normalXPixel) < 0) {
                        normalXPixel = normalXPixel.scale(-1);
                        normalYPixel = normalYPixel.scale(-1);
                    }
                    lastNormalXPixel = normalXPixel;

                    var center = centers.get(j);
                    allVertex.get(i).add(List.of(
                            center.add(-normalXPixel.x - normalYPixel.x, -normalXPixel.y - normalYPixel.y, -normalXPixel.z - normalYPixel.z),
                            center.add(-normalXPixel.x + normalYPixel.x, -normalXPixel.y + normalYPixel.y, -normalXPixel.z + normalYPixel.z),
                            center.add( normalXPixel.x + normalYPixel.x,  normalXPixel.y + normalYPixel.y,  normalXPixel.z + normalYPixel.z),
                            center.add( normalXPixel.x - normalYPixel.x,  normalXPixel.y - normalYPixel.y,  normalXPixel.z - normalYPixel.z)
                    ));
                }
            }

            for (int i = 0; i < 4; i++) {
                var vertexList = allVertex.get(i);
                for (int j = 0; j < vertexList.size() - 1; j++) {
                    var color = colors.get(i + 4 * (j % 2));
                    renderer.addSolidQuad(vertexList.get(j).get(0), vertexList.get(j).get(1), vertexList.get(j + 1).get(1), vertexList.get(j + 1).get(0), color, renderState.lightCoords);
                    renderer.addSolidQuad(vertexList.get(j).get(1), vertexList.get(j).get(2), vertexList.get(j + 1).get(2), vertexList.get(j + 1).get(1), color, renderState.lightCoords);
                    renderer.addSolidQuad(vertexList.get(j).get(2), vertexList.get(j).get(3), vertexList.get(j + 1).get(3), vertexList.get(j + 1).get(2), color, renderState.lightCoords);
                    renderer.addSolidQuad(vertexList.get(j).get(3), vertexList.get(j).get(0), vertexList.get(j + 1).get(0), vertexList.get(j + 1).get(3), color, renderState.lightCoords);
                }
            }
        }
    }

    public static class NetNodeBlockEntityBERS extends BlockEntityRenderState {
        public Direction direction;
        public Map<Direction, BlockPos> connections;
        public Map<Direction, Direction> connectedFaces;
        public Map<Direction, Direction> connectedDirections;
        public BlockPos pos;

    }

    @Override
    public void extractRenderState(NetNodeBlockEntity blockEntity, NetNodeBlockEntityBERS renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        renderState.direction = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getValueOrElse(BasicManaConnector.FACING, Direction.DOWN);
        renderState.connections = blockEntity.getData(ModAttachments.CONNECTIONS);
        renderState.connectedFaces = new HashMap<>();
        renderState.connectedDirections = new HashMap<>();
        renderState.pos = blockEntity.getBlockPos();

        for (var entry : renderState.connections.entrySet()) {
            var connectedPos = entry.getValue();
            var connected = blockEntity.getLevel().getBlockEntity(connectedPos);
            if (connected == null) continue;

            for (var backEntry : connected.getData(ModAttachments.CONNECTIONS).entrySet()) {
                if (!backEntry.getValue().equals(renderState.pos)) continue;
                renderState.connectedFaces.put(entry.getKey(), backEntry.getKey());
                var connectedState = blockEntity.getLevel().getBlockState(connectedPos);
                renderState.connectedDirections.put(entry.getKey(),
                        connectedState.getValueOrElse(BasicManaConnector.FACING, Direction.DOWN));
                break;
            }
        }
    }
}
