/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

import com.almuradev.content.mixin.iface.IMixinTextureAtlasSprite;
import com.almuradev.content.model.obj.geometry.*;
import com.almuradev.content.model.obj.material.MaterialDefinition;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * All quads are baked once, up front, in the constructor. {@link #getQuads} only reads immutable data so it
 * is allocation-free and safe to call from the chunk builder threads.
 */
@SideOnly(Side.CLIENT)
public class OBJBakedModel implements IBakedModel {

    /**
     * Maps [meta][side ordinal] to the face index (of group 0) rendered on that side for simple (6 faced) models.
     * Side ordinals are DOWN, UP, NORTH, SOUTH, WEST, EAST. Metas outside 0-3 render face 0 on every side.
     * Todo: Remove this when we can figure out how to sort out this non-sense.
     */
    private static final int[][] FACE_INDEX_BY_META_AND_SIDE = {
            {5, 0, 4, 2, 1, 3},
            {5, 0, 1, 3, 2, 4},
            {5, 0, 2, 4, 3, 1},
            {5, 0, 3, 1, 4, 2}
    };
    private static final int[] DEFAULT_FACE_INDEX = {0, 0, 0, 0, 0, 0};

    private final OBJModel model;
    private final IModelState state;
    private final VertexFormat format;
    @Nullable
    private final TextureAtlasSprite spriteOverride;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

    // True when no group has exactly 6 faces (or group 0 can't be used per-side); all quads are rendered in the general pass.
    private final boolean isComplex;
    // Every quad of every group.
    private final List<BakedQuad> allQuads;
    // One quad list per face of group 0, only populated for simple models.
    private final List<List<BakedQuad>> simpleFaceQuads;
    private final TextureAtlasSprite particleDiffuseSprite;
    // Block breaking retextures every frame, so keep one model per damage stage sprite.
    private final Map<TextureAtlasSprite, OBJBakedModel> damageModels = new ConcurrentHashMap<>();

    OBJBakedModel(final OBJModel model, final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this(model, state, format, bakedTextureGetter, null);
    }

    OBJBakedModel(final OBJModel model, final IModelState state, final VertexFormat format, final TextureAtlasSprite sprite) {
        this(model, state, format, resourceLocation -> sprite, sprite);
    }

    private OBJBakedModel(final OBJModel model, final IModelState state, final VertexFormat format,
            final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, @Nullable final TextureAtlasSprite spriteOverride) {
        this.model = model;
        this.state = state;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.spriteOverride = spriteOverride;

        final List<Group> groups = model.getGroups();

        boolean hasSixFacedGroup = false;
        for (final Group group : groups) {
            if (group.getFaces().size() == 6) {
                hasSixFacedGroup = true;
                break;
            }
        }
        // Per-side rendering always reads group 0, so it must have all 6 faces.
        this.isComplex = !hasSixFacedGroup || groups.isEmpty() || groups.get(0).getFaces().size() < 6;

        final Baker baker = new Baker();
        final List<BakedQuad> all = new ArrayList<>();
        final List<List<BakedQuad>> perFace = new ArrayList<>();

        for (int g = 0; g < groups.size(); g++) {
            final Group group = groups.get(g);
            final TextureAtlasSprite diffuseSprite = this.resolveDiffuseSprite(group.getMaterialDefinition().orElse(null));

            for (final Face face : group.getFaces()) {
                final BakedQuad quad = baker.bake(face, diffuseSprite);
                all.add(quad);
                if (g == 0 && !this.isComplex) {
                    perFace.add(Collections.singletonList(quad));
                }
            }
        }

        this.allQuads = Collections.unmodifiableList(all);
        this.simpleFaceQuads = Collections.unmodifiableList(perFace);
        // For now, last face = particle generation
        this.particleDiffuseSprite = baker.lastFace == null ? ModelLoader.White.INSTANCE
                : this.createParticleSpriteFor(baker.lastFace, baker.lastSprite);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final ItemCameraTransforms.TransformType cameraTransformType) {
        return PerspectiveMapWrapper.handlePerspective(this, this.state, cameraTransformType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final IBlockState blockState, @Nullable final EnumFacing side, final long rand) {
        // Please note that .obj's pass through [ForgeBlockModelRenderer] class which calculates the light correctly on obj's even if they are rendered in Inventory
        // This is important to know because we are rendering the below on what is usually considered the inventory pass when side == null.
        // Do Not use BlockModelRenderer class as a template for building how this class works!
        // Todo:  for some reason ambient occlusion is not working properly.
        // Note: this method is called 7 times per block.  Side == null, down, up north, south, west, east.

        // Inventory / item rendering.
        if (blockState == null) {
            return this.allQuads;
        }

        // We know that if any block is NOT complex but isOpaque is FALSE, then assume the model facing direction is broken...
        if (this.isComplex || !blockState.isOpaqueCube()) {
            // Complex (or fallback), render everything once in the general pass.
            return side == null ? this.allQuads : Collections.emptyList();
        }

        if (side == null) {
            return Collections.emptyList();
        }

        // Meta values looked up because the facing direction also dictates which face gets rendered and which one doesn't.
        final int meta = blockState.getBlock().getMetaFromState(blockState);
        final int[] faceIndices = meta >= 0 && meta < FACE_INDEX_BY_META_AND_SIDE.length ? FACE_INDEX_BY_META_AND_SIDE[meta] : DEFAULT_FACE_INDEX;
        return this.simpleFaceQuads.get(faceIndices[side.ordinal()]);
    }

    private TextureAtlasSprite resolveDiffuseSprite(@Nullable final MaterialDefinition materialDefinition) {
        TextureAtlasSprite diffuseSprite = this.spriteOverride;

        if (diffuseSprite == null && materialDefinition != null) {
            final ResourceLocation diffuseLocation = materialDefinition.getDiffuseTexture().orElse(null);

            if (diffuseLocation != null) {
                final String path = diffuseLocation.getPath();
                if (path.endsWith(".png")) {
                    final int dot = path.indexOf('.');
                    diffuseSprite = this.bakedTextureGetter.apply(new ResourceLocation(diffuseLocation.getNamespace(), path.substring(0, dot)));
                } else {
                    diffuseSprite = this.bakedTextureGetter.apply(diffuseLocation);
                }
            }
        }

        return diffuseSprite == null ? ModelLoader.White.INSTANCE : diffuseSprite;
    }

    /**
     * Holds the per-model state that is identical for every face, so it is computed once per bake instead of once per face/vertex.
     */
    private final class Baker {

        private final VertexFormatElement.EnumUsage[] usages;
        @Nullable
        private final Matrix4f transform;
        private final Vector4f position = new Vector4f();
        private final Vector4f transformed = new Vector4f();

        @Nullable
        Face lastFace;
        @Nullable
        TextureAtlasSprite lastSprite;

        Baker() {
            final VertexFormat format = OBJBakedModel.this.format;
            this.usages = new VertexFormatElement.EnumUsage[format.getElementCount()];
            for (int e = 0; e < this.usages.length; e++) {
                this.usages[e] = format.getElement(e).getUsage();
            }

            final TRSRTransformation transformation = OBJBakedModel.this.state.apply(Optional.empty()).orElse(null);
            this.transform = transformation == null ? null : transformation.getMatrix();
        }

        BakedQuad bake(final Face face, final TextureAtlasSprite diffuseSprite) {
            final UnpackedBakedQuad.Builder quadBuilder = new UnpackedBakedQuad.Builder(OBJBakedModel.this.format);
            quadBuilder.setContractUVs(true);
            quadBuilder.setTexture(diffuseSprite);
            VertexNormal normal = null;

            for (final VertexDefinition vertexDef : face.getVertices()) {
                if (normal == null) {
                    normal = vertexDef.getNormal().orElse(null);
                }

                for (int e = 0; e < this.usages.length; e++) {
                    switch (this.usages[e]) {
                        case POSITION:
                            final Vertex vertex = vertexDef.getVertex();
                            if (this.transform != null) {
                                this.position.set(vertex.getX(), vertex.getY(), vertex.getZ(), 1f);
                                this.transform.transform(this.position, this.transformed);
                                quadBuilder.put(e, this.transformed.getX(), this.transformed.getY(), this.transformed.getZ());
                            } else {
                                quadBuilder.put(e, vertex.getX(), vertex.getY(), vertex.getZ());
                            }

                            break;
                        case UV:
                            final float u;
                            final float v;

                            if (OBJBakedModel.this.spriteOverride == null) {
                                final VertexTextureCoordinate textureCoordinate = vertexDef.getTextureCoordinate().orElse(null);

                                if (textureCoordinate != null) {
                                    u = textureCoordinate.getU();
                                    v = 1f - textureCoordinate.getV();
                                } else {
                                    u = 0f;
                                    v = 1f;
                                }

                            } else {

                                /*
                                 * 0: (0, 0)       3: (0, 1)
                                 *
                                 *
                                 * 1: (1, 0)       2: (1, 1)
                                 */

                                switch (vertexDef.getIndex()) {
                                    case 1:
                                        u = 0f;
                                        v = 0f;
                                        break;
                                    case 2:
                                        u = 1f;
                                        v = 0f;
                                        break;
                                    case 3:
                                        u = 1f;
                                        v = 1f;
                                        break;
                                    case 4:
                                        u = 0f;
                                        v = 1f;
                                        break;
                                    default:
                                        u = 0f;
                                        v = 0f;
                                }
                            }

                            quadBuilder.put(e, diffuseSprite.getInterpolatedU(u * 16f), diffuseSprite.getInterpolatedV(v * 16f));
                            break;
                        case NORMAL:
                            if (normal != null) {
                                quadBuilder.put(e, normal.getX(), normal.getY(), normal.getZ());
                            }
                            break;
                        case COLOR:
                            quadBuilder.put(e, 1f, 1f, 1f, 1f);
                            break;
                        default:
                            quadBuilder.put(e);
                    }
                }
            }

            if (normal != null) {
                quadBuilder.setQuadOrientation(EnumFacing.getFacingFromVector(normal.getX(), normal.getY(), normal.getZ()));
            }

            this.lastFace = face;
            this.lastSprite = diffuseSprite;
            return quadBuilder.build();
        }
    }

    public OBJBakedModel retextureQuadsFor(final TextureAtlasSprite damageSprite) {
        return this.damageModels.computeIfAbsent(damageSprite, sprite -> new OBJBakedModel(this.model, this.state, this.format, sprite));
    }

    private TextureAtlasSprite createParticleSpriteFor(final Face face, final TextureAtlasSprite diffuseSprite) {

        /*
         * 0: (0, 0)       3: (0, 1)
         *
         *
         * 1: (1, 0)       2: (1, 1)
         */

        final TextureAtlasSprite particleSprite = new TextureAtlasSprite(diffuseSprite.getIconName());
        particleSprite.copyFrom(diffuseSprite);

        final VertexTextureCoordinate vt1 = face.getVertices().get(0).getTextureCoordinate().orElse(null);
        final VertexTextureCoordinate vt3 = face.getVertices().get(2).getTextureCoordinate().orElse(null);

        final float u1;
        final float u2;
        final float v1;
        final float v2;

        if (vt1 != null) {
            u1 = particleSprite.getInterpolatedU(vt1.getU() * 16f);
            v1 = particleSprite.getInterpolatedV((1 - vt1.getV()) * 16f);
        } else {
            u1 = 0f;
            v1 = 0f;
        }

        if (vt3 != null) {
            u2 = particleSprite.getInterpolatedU(vt3.getU() * 16f);
            v2 = particleSprite.getInterpolatedV((1 - vt3.getV()) * 16f);
        } else {
            u2 = 1f;
            v2 = 1f;
        }

        final IMixinTextureAtlasSprite mixinSprite = (IMixinTextureAtlasSprite) particleSprite;
        mixinSprite.setMinU(u1);
        mixinSprite.setMaxU(u2);
        mixinSprite.setMinV(v1);
        mixinSprite.setMaxV(v2);
        return particleSprite;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particleDiffuseSprite;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
