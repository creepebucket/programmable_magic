package org.creepebucket.programmable_magic.spells;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class PackedSpellSpecialRenderer implements SpecialModelRenderer<String> {
    private static final float MIN_Z = 7.5F / 16.0F;
    private static final float MAX_Z = 8.5F / 16.0F;

    private static final Vector3fc EXTENT_0_0_MIN_Z = new Vector3f(0.0F, 0.0F, MIN_Z);
    private static final Vector3fc EXTENT_1_0_MIN_Z = new Vector3f(1.0F, 0.0F, MIN_Z);
    private static final Vector3fc EXTENT_0_1_MIN_Z = new Vector3f(0.0F, 1.0F, MIN_Z);
    private static final Vector3fc EXTENT_1_1_MIN_Z = new Vector3f(1.0F, 1.0F, MIN_Z);
    private static final Vector3fc EXTENT_0_0_MAX_Z = new Vector3f(0.0F, 0.0F, MAX_Z);
    private static final Vector3fc EXTENT_1_0_MAX_Z = new Vector3f(1.0F, 0.0F, MAX_Z);
    private static final Vector3fc EXTENT_0_1_MAX_Z = new Vector3f(0.0F, 1.0F, MAX_Z);
    private static final Vector3fc EXTENT_1_1_MAX_Z = new Vector3f(1.0F, 1.0F, MAX_Z);

    private final SpriteGetter sprites;

    public PackedSpellSpecialRenderer(SpriteGetter sprites) {
        this.sprites = sprites;
    }

    @Override
    public void submit(
            @Nullable String texturePath,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
    ) {
        String normalizedPath = texturePath;
        if (normalizedPath.startsWith("textures/")) {
            normalizedPath = normalizedPath.substring("textures/".length());
        }
        if (normalizedPath.endsWith(".png")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - ".png".length());
        }

        Identifier textureId = normalizedPath.contains(":") ? Identifier.parse(normalizedPath) : Identifier.fromNamespaceAndPath(MODID, normalizedPath);
        SpriteId spriteId = new SpriteId(TextureAtlas.LOCATION_ITEMS, textureId);
        TextureAtlasSprite sprite = this.sprites.get(spriteId);
        Transparency transparency = sprite.transparency();
        RenderType renderType = transparency.hasTranslucent() ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();

        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            submitSpriteQuad(pose, buffer, sprite, MIN_Z, 0, 0, 1, 1, 0, 0, 1, lightCoords, overlayCoords);
            submitSpriteQuad(pose, buffer, sprite, MAX_Z, 0, 0, 1, 1, 0, 0, -1, lightCoords, overlayCoords);
        });
    }

    private static void submitSpriteQuad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            TextureAtlasSprite sprite,
            float z,
            float x0,
            float y0,
            float x1,
            float y1,
            float nx,
            float ny,
            float nz,
            int lightCoords,
            int overlayCoords
    ) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        buffer.addVertex(pose, x0, y0, z).setColor(-1).setUv(u0, v1).setOverlay(overlayCoords).setLight(lightCoords).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x1, y0, z).setColor(-1).setUv(u1, v1).setOverlay(overlayCoords).setLight(lightCoords).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x1, y1, z).setColor(-1).setUv(u1, v0).setOverlay(overlayCoords).setLight(lightCoords).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x0, y1, z).setColor(-1).setUv(u0, v0).setOverlay(overlayCoords).setLight(lightCoords).setNormal(pose, nx, ny, nz);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(EXTENT_0_0_MIN_Z);
        consumer.accept(EXTENT_1_0_MIN_Z);
        consumer.accept(EXTENT_0_1_MIN_Z);
        consumer.accept(EXTENT_1_1_MIN_Z);
        consumer.accept(EXTENT_0_0_MAX_Z);
        consumer.accept(EXTENT_1_0_MAX_Z);
        consumer.accept(EXTENT_0_1_MAX_Z);
        consumer.accept(EXTENT_1_1_MAX_Z);
    }

    @Override
    public @Nullable String extractArgument(ItemStack itemStack) {
        return itemStack.get(org.creepebucket.programmable_magic.registries.ModDataComponents.RESOURCE_LOCATION);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<String> {
        public static final MapCodec<PackedSpellSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new PackedSpellSpecialRenderer.Unbaked());

        @Override
        public @Nullable SpecialModelRenderer<String> bake(SpecialModelRenderer.BakingContext context) {
            return new PackedSpellSpecialRenderer(context.sprites());
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<String>> type() {
            return MAP_CODEC;
        }
    }
}

