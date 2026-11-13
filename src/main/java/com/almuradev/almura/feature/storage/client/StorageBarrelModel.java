/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage.client;

import com.almuradev.content.type.block.type.container.StorageBarrel;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

/**
 * The storage barrel's model: the barrel, with the icon of what's in its first slot on each of its four flat sides. The icon is part of
 * the block's model (no per frame drawing), so a cellar full of barrels costs no more to draw than plain ones.
 */
@SideOnly(Side.CLIENT)
public final class StorageBarrelModel implements IBakedModel {

    // The barrel's four flat sides are 1/16 in from the block's edges. Its texture has a label printed on each (a seed sack on paper):
    // across the side from 0.36 to 0.64 and up it from 0.34 to 0.67. Over it goes plain paper (made from a blank bit of the printed
    // label, so it's the same colour), and on that the icon, each a hair further out so they don't flicker against what's under them
    private static final float SIDE = 1F / 16F;
    private static final float[] PAPER = {0.355F, 0.645F, 0.330F, 0.675F}; // across from, across to, up from, up to
    // Seeds (packets) fill more of the paper; crops and fruit, whose pictures reach their edges, a little less
    private static final float[] SEED_ICON = icon(0.31F);
    private static final float[] CROP_ICON = icon(0.25F);
    private static final float PAPER_OUT = 0.001F;
    private static final float ICON_OUT = 0.002F;
    // The barrel's texture, and a blank bit of its printed label's paper in it (pixels, of 1024)
    private static final String BARREL_TEXTURE = "almura:block/almuracustom1_almuracustom3";
    private static final float[] PAPER_PIXELS = {668F, 672F, 432F, 436F};
    private static final float TEXTURE_SIZE = 1024F;

    private final IBakedModel barrel;
    // The label's quads for each item, made once
    private final Map<String, List<BakedQuad>> labels = new ConcurrentHashMap<>();

    public StorageBarrelModel(final IBakedModel barrel) {
        this.barrel = barrel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
        final List<BakedQuad> quads = this.barrel.getQuads(state, side, rand);
        if (side != null || !(state instanceof IExtendedBlockState)) {
            return quads;
        }
        final ItemStack label = ((IExtendedBlockState) state).getValue(StorageBarrel.LABEL);
        if (label == null || label.isEmpty() || label.getItem().getRegistryName() == null) {
            return quads; // Empty: the printed label shows
        }
        final List<BakedQuad> labelQuads = this.labels.computeIfAbsent(label.getItem().getRegistryName() + "@" + label.getMetadata(),
                k -> this.labelQuads(label));
        if (labelQuads.isEmpty()) {
            return quads;
        }
        final List<BakedQuad> all = new ArrayList<>(quads.size() + labelQuads.size());
        all.addAll(quads);
        all.addAll(labelQuads);
        return all;
    }

    private List<BakedQuad> labelQuads(final ItemStack label) {
        final TextureAtlasSprite icon;
        try {
            icon = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(label, null, null).getParticleTexture();
        } catch (final RuntimeException e) {
            return ImmutableList.of();
        }
        if (icon == null) {
            return ImmutableList.of();
        }
        final List<BakedQuad> quads = new ArrayList<>(8);
        final TextureAtlasSprite barrel = this.barrelTexture();
        if (barrel != null) {
            final float[] paperUv = {
                    barrel.getInterpolatedU(PAPER_PIXELS[0] / TEXTURE_SIZE * 16F), barrel.getInterpolatedU(PAPER_PIXELS[1] / TEXTURE_SIZE * 16F),
                    barrel.getInterpolatedV(PAPER_PIXELS[2] / TEXTURE_SIZE * 16F), barrel.getInterpolatedV(PAPER_PIXELS[3] / TEXTURE_SIZE * 16F)};
            sides(quads, barrel, paperUv, PAPER, PAPER_OUT);
        }
        sides(quads, icon, new float[] {icon.getMinU(), icon.getMaxU(), icon.getMinV(), icon.getMaxV()}, label.getItem() instanceof ItemSeeds ? SEED_ICON : CROP_ICON, ICON_OUT);
        return ImmutableList.copyOf(quads);
    }

    private static float[] icon(final float size) {
        return new float[] {0.5F - size / 2F, 0.5F + size / 2F, 0.5025F - size / 2F, 0.5025F + size / 2F};
    }

    @Nullable
    private TextureAtlasSprite barrelTexture() {
        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(BARREL_TEXTURE);
        // The missing texture comes back when it isn't in the atlas
        return sprite == null || sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite() ? null : sprite;
    }

    /**
     * Adds a rectangle on each of the barrel's four flat sides.
     *
     * @param uv the texture's left, right, top and bottom
     * @param area across from, across to, up from, up to, on the side
     */
    private static void sides(final List<BakedQuad> quads, final TextureAtlasSprite sprite, final float[] uv, final float[] area, final float out) {
        final float a0 = area[0], a1 = area[1], y0 = area[2], y1 = area[3];
        final float s = 1 - SIDE + out, n = SIDE - out;
        // Each side's corners: top left, bottom left, bottom right, top right, as seen from outside the barrel
        quads.add(quad(sprite, uv, EnumFacing.SOUTH, new float[][] {{a0, y1, s}, {a0, y0, s}, {a1, y0, s}, {a1, y1, s}}));
        quads.add(quad(sprite, uv, EnumFacing.NORTH, new float[][] {{1 - a0, y1, n}, {1 - a0, y0, n}, {1 - a1, y0, n}, {1 - a1, y1, n}}));
        quads.add(quad(sprite, uv, EnumFacing.EAST, new float[][] {{s, y1, 1 - a0}, {s, y0, 1 - a0}, {s, y0, 1 - a1}, {s, y1, 1 - a1}}));
        quads.add(quad(sprite, uv, EnumFacing.WEST, new float[][] {{n, y1, a0}, {n, y0, a0}, {n, y0, a1}, {n, y1, a1}}));
    }

    private static BakedQuad quad(final TextureAtlasSprite sprite, final float[] uv, final EnumFacing face, final float[][] corners) {
        final VertexFormat format = DefaultVertexFormats.ITEM;
        final UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(face);
        builder.setTexture(sprite);
        builder.setApplyDiffuseLighting(true);
        // The texture's corners in the same order: top left, bottom left, bottom right, top right
        final float[][] uvs = {{uv[0], uv[2]}, {uv[0], uv[3]}, {uv[1], uv[3]}, {uv[1], uv[2]}};
        for (int vertex = 0; vertex < 4; vertex++) {
            for (int element = 0; element < format.getElementCount(); element++) {
                final VertexFormatElement e = format.getElement(element);
                switch (e.getUsage()) {
                    case POSITION:
                        builder.put(element, corners[vertex][0], corners[vertex][1], corners[vertex][2], 1F);
                        break;
                    case COLOR:
                        builder.put(element, 1F, 1F, 1F, 1F);
                        break;
                    case UV:
                        if (e.getIndex() == 0) {
                            builder.put(element, uvs[vertex][0], uvs[vertex][1], 0F, 1F);
                        } else {
                            builder.put(element);
                        }
                        break;
                    case NORMAL:
                        builder.put(element, face.getXOffset(), face.getYOffset(), face.getZOffset(), 0F);
                        break;
                    default:
                        builder.put(element);
                }
            }
        }
        return builder.build();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.barrel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.barrel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.barrel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.barrel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.barrel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.barrel.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final ItemCameraTransforms.TransformType type) {
        return this.barrel.handlePerspective(type);
    }
}
