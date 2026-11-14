/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.client;

import com.almuradev.almura.content.registry.ContentContainers;
import com.almuradev.almura.content.registry.ContentSeed;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The storage barrel's label, as 3.5's {@code StorageBarrelModel} drew it: on each of the barrel's four flat sides, over its printed label,
 * plain paper (a blank bit of that label, so it's the same colour) and on it the icon of what the barrel holds first. Seeds (packets) fill
 * more of the paper; crops and fruit, whose pictures reach their edges, a little less. An empty barrel shows its printed label.
 */
public final class StorageBarrelRenderer implements BlockEntityRenderer<ContentContainers.ContainerBlockEntity, StorageBarrelRenderer.State> {

    // The barrel's four flat sides are 1/16 in from the block's edges. Its texture's label is across the side from 0.36 to 0.64 and up it
    // from 0.34 to 0.67; the paper over it, and the icon on that, each a hair further out so they don't flicker against what's under them
    private static final float SIDE = 1F / 16F;
    private static final float[] PAPER = {0.355F, 0.645F, 0.330F, 0.675F}; // across from, across to, up from, up to
    private static final float[] SEED_ICON = icon(0.31F);
    private static final float[] CROP_ICON = icon(0.25F);
    private static final float PAPER_OUT = 0.001F;
    private static final float ICON_OUT = 0.002F;
    // The barrel's texture, and a blank bit of its printed label's paper in it (pixels, of 1024)
    private static final SpriteId BARREL_TEXTURE = new SpriteId(TextureAtlas.LOCATION_BLOCKS,
        Identifier.fromNamespaceAndPath("almura", "block/almuracustom1_almuracustom3"));
    private static final float[] PAPER_PIXELS = {668F, 672F, 432F, 436F};
    private static final float TEXTURE_SIZE = 1024F;

    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;
    private final RandomSource random = RandomSource.create();

    public StorageBarrelRenderer(final BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.sprites = context.sprites();
    }

    public static final class State extends BlockEntityRenderState {

        @Nullable TextureAtlasSprite icon;
        boolean seed;
    }

    private static float[] icon(final float size) {
        return new float[] {0.5F - size / 2F, 0.5F + size / 2F, 0.5025F - size / 2F, 0.5025F + size / 2F};
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(final ContentContainers.ContainerBlockEntity barrel, final State state, final float partialTicks,
        final Vec3 cameraPosition, final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(barrel, state, partialTicks, cameraPosition, breakProgress);
        state.icon = null;
        final ItemStack label = barrel.label();
        if (label.isEmpty() || !ContentContainers.STORAGE_BARREL.equals(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(
            barrel.getBlockState().getBlock()))) {
            return;
        }
        // The item's icon: the picture of it its model gives (its particle texture, as 3.5 took it)
        final ItemStackRenderState item = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(item, label, ItemDisplayContext.GUI, barrel.getLevel(), null, 0);
        final Material.Baked material = item.pickParticleMaterial(this.random);
        state.icon = material == null ? null : material.sprite();
        state.seed = label.getItem() instanceof ContentSeed || label.getItem() instanceof final BlockItem block && block.getBlock() instanceof CropBlock;
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        final TextureAtlasSprite icon = state.icon;
        if (icon == null) {
            return;
        }
        final TextureAtlasSprite barrel = this.sprites.get(BARREL_TEXTURE);
        final int light = state.lightCoords;
        if (barrel != null && !barrel.contents().name().getPath().equals("missingno")) {
            final float[] paperUv = {barrel.getU(PAPER_PIXELS[0] / TEXTURE_SIZE), barrel.getU(PAPER_PIXELS[1] / TEXTURE_SIZE),
                barrel.getV(PAPER_PIXELS[2] / TEXTURE_SIZE), barrel.getV(PAPER_PIXELS[3] / TEXTURE_SIZE)};
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(barrel.atlasLocation()),
                (pose, buffer) -> sides(pose, buffer, paperUv, PAPER, PAPER_OUT, light));
        }
        final float[] iconUv = {icon.getU0(), icon.getU1(), icon.getV0(), icon.getV1()};
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(icon.atlasLocation()),
            (pose, buffer) -> sides(pose, buffer, iconUv, state.seed ? SEED_ICON : CROP_ICON, ICON_OUT, light));
    }

    /**
     * A rectangle on each of the barrel's four flat sides.
     *
     * @param uv the texture's left, right, top and bottom
     * @param area across from, across to, up from, up to, on the side
     */
    private static void sides(final PoseStack.Pose pose, final VertexConsumer buffer, final float[] uv, final float[] area, final float out,
        final int light) {
        final float a0 = area[0], a1 = area[1], y0 = area[2], y1 = area[3];
        final float s = 1 - SIDE + out, n = SIDE - out;
        // Each side's corners: top left, bottom left, bottom right, top right, as seen from outside the barrel
        quad(pose, buffer, uv, 0, 0, 1, new float[][] {{a0, y1, s}, {a0, y0, s}, {a1, y0, s}, {a1, y1, s}}, light);
        quad(pose, buffer, uv, 0, 0, -1, new float[][] {{1 - a0, y1, n}, {1 - a0, y0, n}, {1 - a1, y0, n}, {1 - a1, y1, n}}, light);
        quad(pose, buffer, uv, 1, 0, 0, new float[][] {{s, y1, 1 - a0}, {s, y0, 1 - a0}, {s, y0, 1 - a1}, {s, y1, 1 - a1}}, light);
        quad(pose, buffer, uv, -1, 0, 0, new float[][] {{n, y1, a0}, {n, y0, a0}, {n, y0, a1}, {n, y1, a1}}, light);
    }

    private static void quad(final PoseStack.Pose pose, final VertexConsumer buffer, final float[] uv, final float nx, final float ny,
        final float nz, final float[][] corners, final int light) {
        // The texture's corners in the same order: top left, bottom left, bottom right, top right
        final float[][] uvs = {{uv[0], uv[2]}, {uv[0], uv[3]}, {uv[1], uv[3]}, {uv[1], uv[2]}};
        for (int vertex = 0; vertex < 4; vertex++) {
            buffer.addVertex(pose, corners[vertex][0], corners[vertex][1], corners[vertex][2])
                .setColor(0xFFFFFFFF)
                .setUv(uvs[vertex][0], uvs[vertex][1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
        }
    }
}
