package org.creepebucket.programmable_magic.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final int[] NO_TINT_LAYERS = new int[0];

    private final MaterialSet materials;
    private final Map<Identifier, List<BakedQuad>> bakedQuads = new HashMap<>();

    public PackedSpellSpecialRenderer(MaterialSet materials) {
        this.materials = materials;
    }

    @Override
    public void submit(
            @Nullable String texturePath,
            ItemDisplayContext displayContext,
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

        Identifier spriteId = normalizedPath.contains(":")
                ? Identifier.parse(normalizedPath)
                : Identifier.fromNamespaceAndPath(MODID, normalizedPath);

        List<BakedQuad> quads = this.bakedQuads.get(spriteId);
        if (quads == null) {
            Material material = new Material(TextureAtlas.LOCATION_ITEMS, spriteId);
            TextureAtlasSprite sprite = this.materials.get(material);
            List<BlockElement> elements = UnbakedElementsHelper.createUnbakedItemElements(0, sprite);
            quads = UnbakedElementsHelper.bakeElements(elements, $ -> sprite, BlockModelRotation.IDENTITY);
            this.bakedQuads.put(spriteId, quads);
        }

        collector.submitItem(
                poseStack,
                displayContext,
                lightCoords,
                overlayCoords,
                outlineColor,
                NO_TINT_LAYERS,
                quads,
                Sheets.translucentItemSheet(),
                hasFoil ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE
        );
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
        return itemStack.get(ModDataComponents.RESOURCE_LOCATION);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<PackedSpellSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new PackedSpellSpecialRenderer.Unbaked());

        @Override
        public @Nullable SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new PackedSpellSpecialRenderer(context.materials());
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
