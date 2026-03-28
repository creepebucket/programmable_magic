package org.creepebucket.programmable_magic.renderer.api;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class RenderHelper {

    // 渲染管线...
    public static final RenderPipeline SOLID_FACE_PIPELINE =
            RenderPipeline.builder(              RenderPipelines.MATRICES_FOG_SNIPPET)
                          .withLocation(         Identifier.fromNamespaceAndPath(MODID, "pipeline/test_node_solid_faces"))
                          .withVertexShader(     "core/rendertype_leash")
                          .withFragmentShader(   "core/rendertype_leash")
                          .withSampler(          "Sampler2")
                          .withDepthWrite(       true)
                          .withCull(             false)
                          .withVertexFormat(     DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLES)
                          .build();

    // 渲染类型...
    public static final RenderType LINES        = RenderType.create("pm_lines",        RenderSetup.builder(RenderPipelines.LINES   ).createRenderSetup());
    public static final RenderType SOLID_FACE   = RenderType.create("pm_solid_face",   RenderSetup.builder(SOLID_FACE_PIPELINE     ).useLightmap().createRenderSetup());

    public PoseStack poseStack;
    public SubmitNodeCollector collector;
    public CameraRenderState cameraState;

    public RenderHelper(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.poseStack = poseStack;
        this.collector = submitNodeCollector;
        this.cameraState = cameraRenderState;
    }

    public void addLine(Vec3 p1, Vec3 p2, int color, float width) {

        float x1 = (float) p1.x, x2 = (float) p2.x, y1 = (float) p1.y, y2 = (float) p2.y, z1 = (float) p1.z, z2 = (float) p2.z;

        // 计算法线方向
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        float finalDx = dx, finalDy = dy, finalDz = dz;

        collector.submitCustomGeometry(poseStack, LINES, ((pose, consumer) -> {
            consumer.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, finalDx, finalDy, finalDz).setLineWidth(width);
            consumer.addVertex(pose, x2, y2, z2).setColor(color).setNormal(pose, finalDx, finalDy, finalDz).setLineWidth(width);
        }));
    }

    public void addSolidQuad(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int color, int light) {

        float x1 = (float) p0.x, x2 = (float) p1.x, y1 = (float) p0.y, y2 = (float) p1.y, z1 = (float) p0.z, z2 = (float) p1.z;
        float x3 = (float) p2.x, x4 = (float) p3.x, y3 = (float) p2.y, y4 = (float) p3.y, z3 = (float) p2.z, z4 = (float) p3.z;

        float ux = x2 - x1, uy = y2 - y1, uz = z2 - z1, vx = x3 - x1, vy = y3 - y1, vz = z3 - z1;
        float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        float invLen = (float) (1.0 / Math.sqrt(nx * nx + ny * ny + nz * nz));
        nx *= invLen;
        ny *= invLen;
        nz *= invLen;

        float finalNx = nx, finalNy = ny, finalNz = nz;
        collector.submitCustomGeometry(poseStack, SOLID_FACE, ((pose, consumer) -> {

            // 第一个三角形：v1, v2, v3
            consumer.addVertex(pose, x1, y1, z1).setColor(color).setLight(light).setNormal(pose, finalNx, finalNy, finalNz);
            consumer.addVertex(pose, x2, y2, z2).setColor(color).setLight(light).setNormal(pose, finalNx, finalNy, finalNz);
            consumer.addVertex(pose, x3, y3, z3).setColor(color).setLight(light).setNormal(pose, finalNx, finalNy, finalNz);

            // 第二个三角形：v1, v3, v4
            consumer.addVertex(pose, x1, y1, z1).setColor(color).setLight(light).setNormal(pose, finalNx, finalNy, finalNz);
            consumer.addVertex(pose, x3, y3, z3).setColor(color).setLight(light).setNormal(pose, finalNx, finalNy, finalNz);
            consumer.addVertex(pose, x4, y4, z4).setColor(color).setLight(light).setNormal(pose, finalNx, finalNy, finalNz);
        }));
    }

    public void addSolidQuad(Vec3[] quad, int color, int light) {
        addSolidQuad(quad[0], quad[1], quad[2], quad[3], color, light);
    }

    public void addSolidQuad(List<Vec3> quad, int color, int light) {
        addSolidQuad(quad.toArray(new Vec3[4]), color, light);
    }

    public void addAxisAlienedSolidQuad(Vec3 pos, Vec3 size, int color, int light) {
        Vec3 p1 = pos, p2 = pos.add(size), p3, p4;

        // 对p1, p2所处平面状态进行判断, 计算p3, p4
        if (size.x == 0) {
            p3 = pos.add(0, size.y, 0);
            p4 = pos.add(0, 0, size.z);
        } else if (size.y == 0) {
            p3 = pos.add(size.x, 0, 0);
            p4 = pos.add(0, 0, size.z);
        } else {
            p3 = pos.add(size.x, 0, 0);
            p4 = pos.add(0, size.y, 0);
        }

        addSolidQuad(p1, p3, p2, p4, color, light);
    }

    public void addText(Vec3 pos, Quaternionf direction, float size, int color, int light, FormattedCharSequence text) {
        poseStack.pushPose();
        poseStack.translate(pos);

        poseStack.mulPose(direction);
        poseStack.scale(size, -size, size);

        Font font = Minecraft.getInstance().font;
        float x = -font.width(text) / 2.0F;
        float y = -font.lineHeight / 2.0F;
        collector.submitText(poseStack, x, y, text, false, Font.DisplayMode.POLYGON_OFFSET, light, color, 0, 0);
        poseStack.pushPose();
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        collector.submitText(poseStack, x, y, text, false, Font.DisplayMode.POLYGON_OFFSET, light, color, 0, 0);
        poseStack.popPose();
        poseStack.popPose();
    }
    
    public void addText(Vec3 pos, Direction direction, float size, int color, int light, FormattedCharSequence text) {
        Quaternionf dir = null;
        
        switch (direction) {
            case SOUTH -> dir = Axis.YP.rotationDegrees( 0F);
            case NORTH -> dir = Axis.YP.rotationDegrees( 180F);
            case WEST  -> dir = Axis.YP.rotationDegrees(-90F);
            case EAST  -> dir = Axis.YP.rotationDegrees( 90F);
            case UP    -> dir = Axis.XP.rotationDegrees( 90F);
            case DOWN  -> dir = Axis.XP.rotationDegrees(-90F);
        }
        
        addText(pos, dir, size, color, light, text);
    }
}
