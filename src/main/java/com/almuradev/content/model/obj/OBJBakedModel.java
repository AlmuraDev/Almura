/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

import com.almuradev.content.mixin.iface.IMixinTextureAtlasSprite;
import com.almuradev.content.model.obj.geometry.Face;
import com.almuradev.content.model.obj.geometry.Group;
import com.almuradev.content.model.obj.geometry.Vertex;
import com.almuradev.content.model.obj.geometry.VertexDefinition;
import com.almuradev.content.model.obj.geometry.VertexNormal;
import com.almuradev.content.model.obj.geometry.VertexTextureCoordinate;
import com.almuradev.content.model.obj.material.MaterialDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

@SideOnly(Side.CLIENT)
public class OBJBakedModel implements IBakedModel {

    private final OBJModel model;
    private final IModelState state;
    private final VertexFormat format;
    @Nullable
    private TextureAtlasSprite spriteOverride;
    @Nullable
    private List<BakedQuad> quadCache;
    private Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private TextureAtlasSprite particleDiffuseSprite = ModelLoader.White.INSTANCE;


    OBJBakedModel(final OBJModel model, final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.model = model;
        this.state = state;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.spriteOverride = null;
    }

    OBJBakedModel(final OBJModel mode, final IModelState state, final VertexFormat format, final TextureAtlasSprite sprite) {
        this.model = mode;
        this.state = state;
        this.format = format;
        this.bakedTextureGetter = resourceLocation -> sprite;
        this.spriteOverride = sprite;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final ItemCameraTransforms.TransformType cameraTransformType) {
        return PerspectiveMapWrapper.handlePerspective(this, this.state, cameraTransformType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final IBlockState blockState, @Nullable final EnumFacing side, final long rand) {
        boolean isComplex = true;
        boolean fallback = false;
        for (Group group : this.model.getGroups()) {
            if (group.getFaces().size() == 6) {
                isComplex = false;
                break;
            }
        }

        if (isComplex && this.quadCache != null) {
            // Skip re-calculating quads as they are cached.
            return this.quadCache;
        }

        if (blockState != null) {
            if (side != null && isComplex) {
                // Skip rendering
                return new LinkedList<>();
            }
        }

        final List<BakedQuad> quads = new ArrayList<>();

        if (blockState != null && !isComplex) {
            if (!blockState.isOpaqueCube()) { // We know that if any block is NOT complex but isOpague is FALL, then assume the model facing direction is broken...
                fallback = true;
            }
        }

        if (blockState == null || isComplex || fallback) {
            // Complex, return all.
            this.model.getGroups().forEach(group -> {
                final MaterialDefinition materialDefinition = group.getMaterialDefinition().orElse(null);
                group.getFaces().forEach(face -> this.populateQuadsByFace(materialDefinition, face, quads));
            });
            this.quadCache = quads;
        } else if (side != null) {
            final Group group = this.model.getGroups().get(0);
            if (group != null) {
                final MaterialDefinition materialDefinition = group.getMaterialDefinition().orElse(null);
                int meta = blockState.getBlock().getMetaFromState(blockState);
                int index = 0;
                // Meta values looked up because the facing direction also dictates which face gets rendered and which one doesn't.
                // Todo: Remove this when we can figure out how to sort out this non-sense.
                if (meta == 0) {
                    switch (side) {
                        case DOWN:
                            index = 5;
                            break;
                        case UP:
                            index = 0;
                            break;
                        case NORTH:
                            index = 4;
                            break;
                        case SOUTH:
                            index = 2;
                            break;
                        case WEST:
                            index = 1;
                            break;
                        case EAST:
                            index = 3;
                            break;
                    }
                }

                if (meta == 1) {
                    switch (side) {
                        case DOWN:
                            index = 5;
                            break;
                        case UP:
                            index = 0;
                            break;
                        case NORTH:
                            index = 1;
                            break;
                        case SOUTH:
                            index = 3;
                            break;
                        case WEST:
                            index = 2;
                            break;
                        case EAST:
                            index = 4;
                            break;
                    }
                }

                if (meta == 2) {
                    switch (side) {
                        case DOWN:
                            index = 5;
                            break;
                        case UP:
                            index = 0;
                            break;
                        case NORTH:
                            index = 2;
                            break;
                        case SOUTH:
                            index = 4;
                            break;
                        case WEST:
                            index = 3;
                            break;
                        case EAST:
                            index = 1;
                            break;
                    }
                }

                if (meta == 3) {
                    switch (side) {
                        case DOWN:
                            index = 5;
                            break;
                        case UP:
                            index = 0;
                            break;
                        case NORTH:
                            index = 3;
                            break;
                        case SOUTH:
                            index = 1;
                            break;
                        case WEST:
                            index = 4;
                            break;
                        case EAST:
                            index = 2;
                            break;
                    }
                }

                final Face face = group.getFaces().get(index);
                if (face != null) {
                    this.populateQuadsByFace(materialDefinition, face, quads);
                }
            }
        }
        return quads;
    }

    private void populateQuadsByFace(@Nullable MaterialDefinition materialDefinition, Face face, List<BakedQuad> quads) {
        Face particleFace;
        TextureAtlasSprite particleAtlasSprite;

        final TRSRTransformation transformation = this.state.apply(Optional.empty()).orElse(null);

        TextureAtlasSprite diffuseSprite = this.spriteOverride;

        if (diffuseSprite == null) {
            if (materialDefinition != null) {
                if (materialDefinition.getDiffuseTexture().isPresent()) {
                    final ResourceLocation diffuseLocation = materialDefinition.getDiffuseTexture().orElse(null);

                    if (diffuseLocation != null) {

                        if (diffuseLocation.getPath().endsWith(".png")) {
                            diffuseSprite = this.bakedTextureGetter.apply(new ResourceLocation(diffuseLocation.getNamespace(), diffuseLocation.getPath().split("\\.")[0]));
                        } else {
                            diffuseSprite = this.bakedTextureGetter.apply(diffuseLocation);
                        }
                    }
                }
            }
        }

        if (diffuseSprite == null) {
            diffuseSprite = ModelLoader.White.INSTANCE;
        }

        particleAtlasSprite = diffuseSprite;

        particleFace = face;

        final UnpackedBakedQuad.Builder quadBuilder = new UnpackedBakedQuad.Builder(this.format);
        quadBuilder.setContractUVs(true);
        quadBuilder.setTexture(diffuseSprite);
        VertexNormal normal = null;

        for (final VertexDefinition vertexDef : face.getVertices()) {
            if (normal == null) {
                normal = vertexDef.getNormal().orElse(null);
            }

            for (int e = 0; e < this.format.getElementCount(); e++) {
                switch (this.format.getElement(e).getUsage()) {
                    case POSITION:
                        final Vertex vertex = vertexDef.getVertex();
                        if (transformation != null) {
                            final Matrix4f transform = transformation.getMatrix();
                            final Vector4f position = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1f);
                            final Vector4f transformed = new Vector4f();
                            transform.transform(position, transformed);
                            quadBuilder.put(e, transformed.getX(), transformed.getY(), transformed.getZ());
                        } else {
                            quadBuilder.put(e, vertex.getX(), vertex.getY(), vertex.getZ());
                        }

                        break;
                    case UV:
                        final float u;
                        final float v;

                        if (this.spriteOverride == null) {
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

        quads.add(quadBuilder.build());

        if (particleFace != null && particleAtlasSprite != null) {
            // For now, last face = particle generation
            this.particleDiffuseSprite = this.createParticleSpriteFor(particleFace, particleAtlasSprite);
        }
    }

    public OBJBakedModel retextureQuadsFor(final TextureAtlasSprite damageSprite) {
        return new OBJBakedModel(this.model, this.state, this.format, damageSprite);
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
