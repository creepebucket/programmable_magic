package org.creepebucket.programmable_magic.events.machines;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.creepebucket.programmable_magic.mananet.mechines.BasicMachine;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.GeckoLibResources;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.cache.model.GeoQuad;
import software.bernie.geckolib.cache.model.GeoVertex;
import software.bernie.geckolib.cache.model.cuboid.CuboidGeoBone;
import software.bernie.geckolib.cache.model.cuboid.GeoCube;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 纯ai
 */
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BasicMachinePlacementPreview {

    @SubscribeEvent
    public static void on_render_level(RenderLevelStageEvent.AfterEntities event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.level == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit)) return;
        if (hit.getType() == HitResult.Type.MISS) return;

        BasicMachineItemHelper.Held held = BasicMachineItemHelper.get_held_basic_machine(mc.player);
        if (held == null) return;
        BlockItem block_item = held.block_item();
        ItemStack stack = held.stack();
        InteractionHand hand = held.hand();

        BlockPlaceContext context = new BlockPlaceContext(mc.player, hand, stack, hit);
        BlockPos pos = context.getClickedPos();
        float line_width = mc.getWindow().getAppropriateLineWidth();
        boolean can_place = context.canPlace();
        BlockState placement_state = block_item.getBlock().getStateForPlacement(context);
        BlockState render_state = placement_state != null ? placement_state : block_item.getBlock().defaultBlockState();
        if (placement_state == null) can_place = false;
        if (block_item.getBlock() instanceof BasicMachine machine) {
            for (var offset : machine.DUMMY_OFFSETS) {
                if (!mc.level.getBlockState(pos.offset(offset)).canBeReplaced()) {
                    can_place = false;
                    break;
                }
            }
        }
        if (placement_state != null) {
            if (!placement_state.canSurvive(mc.level, pos)) can_place = false;
            if (!mc.level.isUnobstructed(placement_state, pos, CollisionContext.placementContext(mc.player))) can_place = false;
        }
        if (render_state.getRenderShape() == RenderShape.INVISIBLE) return;
        int color = can_place ? ARGB.colorFromFloat(0.45F, 1.0F, 1.0F, 1.0F) : ARGB.colorFromFloat(0.45F, 1.0F, 0.0F, 0.0F);

        boolean rendered = false;
        var buffer = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderTypes.lines());
        PoseStack pose_stack = event.getPoseStack();
        Vec3 cam = event.getLevelRenderState().cameraRenderState.pos;
        pose_stack.pushPose();
        pose_stack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);

        BlockEntity block_entity = null;
        if (block_item.getBlock() instanceof EntityBlock entity_block) block_entity = entity_block.newBlockEntity(pos, render_state);
        if (block_entity instanceof GeoAnimatable geo_animatable) rendered = try_render_gecko_model(mc, render_state, block_entity, geo_animatable, pose_stack, consumer, line_width, color);
        if (!rendered && render_state.getRenderShape() == RenderShape.MODEL) rendered = try_render_vanilla_model(mc.level, pos, render_state, pose_stack, consumer, line_width, color);

        pose_stack.popPose();
        if (rendered) buffer.endLastBatch();
    }

    private static boolean try_render_vanilla_model(Level level, BlockPos pos, BlockState state, PoseStack pose_stack, VertexConsumer consumer, float line_width, int color) {
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        RandomSource random = RandomSource.create();
        random.setSeed(state.getSeed(pos));
        LongOpenHashSet edges = new LongOpenHashSet();
        List<BlockModelPart> parts = new ArrayList<>();
        model.collectParts(level, pos, state, random, parts);
        for (BlockModelPart part : parts) {
            render_vanilla_part(part, pose_stack, consumer, line_width, color, edges);
        }
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean try_render_gecko_model(Minecraft mc, BlockState state, BlockEntity block_entity, GeoAnimatable geo_animatable, PoseStack pose_stack, VertexConsumer consumer, float line_width, int color) {
        GeoBlockRenderer geo_renderer = RenderUtil.getGeckoLibBlockRenderer(block_entity.getType());
        if (geo_renderer == null) return false;

        GeoRenderState render_state = (GeoRenderState) geo_renderer.createRenderState();
        geo_renderer.fillRenderState(geo_animatable, null, render_state, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));

        BakedGeoModel model = GeckoLibResources.getBakedModels().getModel(geo_renderer.getGeoModel().getModelResource(render_state));
        pose_stack.pushPose();
        pose_stack.translate(0.5d, 0, 0.5d);
        rotate_for_facing(pose_stack, render_state.getOrDefaultGeckolibData(DataTickets.BLOCK_FACING, Direction.NORTH));
        render_gecko_model(model, pose_stack, consumer, line_width, color);
        pose_stack.popPose();
        return true;
    }

    private static void rotate_for_facing(PoseStack pose_stack, Direction facing) {
        switch (facing) {
            case SOUTH -> pose_stack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pose_stack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> pose_stack.mulPose(Axis.YN.rotationDegrees(90));
            case UP -> pose_stack.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> pose_stack.mulPose(Axis.XN.rotationDegrees(90));
            default -> {
            }
        }
    }

    private static void render_vanilla_part(BlockModelPart part, PoseStack pose_stack, VertexConsumer consumer, float line_width, int color, LongOpenHashSet edges) {
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : part.getQuads(direction)) {
                render_vanilla_quad(quad, pose_stack, consumer, line_width, color, edges);
            }
        }
        for (BakedQuad quad : part.getQuads(null)) {
            render_vanilla_quad(quad, pose_stack, consumer, line_width, color, edges);
        }
    }

    private static void render_vanilla_quad(BakedQuad quad, PoseStack pose_stack, VertexConsumer consumer, float line_width, int color, LongOpenHashSet edges) {
        var v0 = quad.position0();
        var v1 = quad.position1();
        var v2 = quad.position2();
        var v3 = quad.position3();
        render_quad_edges(
                pose_stack,
                consumer,
                v0.x(), v0.y(), v0.z(),
                v1.x(), v1.y(), v1.z(),
                v2.x(), v2.y(), v2.z(),
                v3.x(), v3.y(), v3.z(),
                line_width,
                color,
                edges
        );
    }

    private static void render_gecko_model(BakedGeoModel model, PoseStack pose_stack, VertexConsumer consumer, float line_width, int color) {
        LongOpenHashSet edges = new LongOpenHashSet();
        for (GeoBone bone : model.topLevelBones()) {
            pose_stack.pushPose();
            RenderUtil.prepMatrixForBone(pose_stack, bone);
            render_gecko_bone(bone, pose_stack, consumer, line_width, color, edges);
            pose_stack.popPose();
        }
    }

    private static void render_gecko_bone(GeoBone bone, PoseStack pose_stack, VertexConsumer consumer, float line_width, int color, LongOpenHashSet edges) {
        if (bone instanceof CuboidGeoBone cuboid_bone) {
            for (GeoCube cube : cuboid_bone.cubes) {
                if (cube.quads() == null) continue;
                pose_stack.pushPose();
                cube.translateToPivotPoint(pose_stack);
                cube.rotate(pose_stack);
                cube.translateAwayFromPivotPoint(pose_stack);
                for (GeoQuad quad : cube.quads()) {
                    if (quad == null) continue;
                    GeoVertex[] vertices = quad.vertices();
                    render_quad_edges(
                            pose_stack,
                            consumer,
                            vertices[0].posX(), vertices[0].posY(), vertices[0].posZ(),
                            vertices[1].posX(), vertices[1].posY(), vertices[1].posZ(),
                            vertices[2].posX(), vertices[2].posY(), vertices[2].posZ(),
                            vertices[3].posX(), vertices[3].posY(), vertices[3].posZ(),
                            line_width,
                            color,
                            edges
                    );
                }
                pose_stack.popPose();
            }
        }

        for (GeoBone child : bone.children()) {
            pose_stack.pushPose();
            RenderUtil.prepMatrixForBone(pose_stack, child);
            render_gecko_bone(child, pose_stack, consumer, line_width, color, edges);
            pose_stack.popPose();
        }
    }

    private static void render_quad_edges(
            PoseStack pose_stack,
            VertexConsumer consumer,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float line_width,
            int color,
            LongOpenHashSet edges
    ) {
        render_edge(pose_stack, consumer, x0, y0, z0, x1, y1, z1, line_width, color, edges);
        render_edge(pose_stack, consumer, x1, y1, z1, x2, y2, z2, line_width, color, edges);
        render_edge(pose_stack, consumer, x2, y2, z2, x3, y3, z3, line_width, color, edges);
        render_edge(pose_stack, consumer, x3, y3, z3, x0, y0, z0, line_width, color, edges);
    }

    private static void render_edge(PoseStack pose_stack, VertexConsumer consumer, float ax, float ay, float az, float bx, float by, float bz, float line_width, int color, LongOpenHashSet edges) {
        PoseStack.Pose pose = pose_stack.last();
        var mat = pose.pose();
        float tax = mat.m00() * ax + mat.m10() * ay + mat.m20() * az + mat.m30();
        float tay = mat.m01() * ax + mat.m11() * ay + mat.m21() * az + mat.m31();
        float taz = mat.m02() * ax + mat.m12() * ay + mat.m22() * az + mat.m32();
        float tbx = mat.m00() * bx + mat.m10() * by + mat.m20() * bz + mat.m30();
        float tby = mat.m01() * bx + mat.m11() * by + mat.m21() * bz + mat.m31();
        float tbz = mat.m02() * bx + mat.m12() * by + mat.m22() * bz + mat.m32();

        long key = edge_key(tax, tay, taz, tbx, tby, tbz);
        if (edges.contains(key)) return;
        edges.add(key);

        Vector3f normal = new Vector3f(bx - ax, by - ay, bz - az).normalize();
        consumer.addVertex(pose, ax, ay, az).setColor(color).setNormal(pose, normal).setLineWidth(line_width);
        consumer.addVertex(pose, bx, by, bz).setColor(color).setNormal(pose, normal).setLineWidth(line_width);
    }

    private static long edge_key(float ax, float ay, float az, float bx, float by, float bz) {
        long a = vertex_key(ax, ay, az);
        long b = vertex_key(bx, by, bz);
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return min * 31L + max;
    }

    private static long vertex_key(float x, float y, float z) {
        long h = 0xcbf29ce484222325L;
        h = (h ^ Math.round(x * 4096f)) * 0x100000001b3L;
        h = (h ^ Math.round(y * 4096f)) * 0x100000001b3L;
        h = (h ^ Math.round(z * 4096f)) * 0x100000001b3L;
        return h;
    }
}
