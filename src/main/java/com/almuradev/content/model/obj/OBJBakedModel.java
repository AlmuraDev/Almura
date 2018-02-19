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
    private Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    @Nullable
    private List<BakedQuad> quads;
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
    public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {

        if (this.quads == null) {
            this.quads = new LinkedList<>();

            final TRSRTransformation transformation = this.state.apply(Optional.empty()).orElse(null);

            Face particleFace = null;
            TextureAtlasSprite particleAtlasSprite = null;

            for (final Group group : this.model.getGroups()) {
                final MaterialDefinition materialDefinition = group.getMaterialDefinition().orElse(null);

                TextureAtlasSprite diffuseSprite = this.spriteOverride;

                if (diffuseSprite == null) {
                    if (materialDefinition != null) {
                        if (materialDefinition.getDiffuseTexture().isPresent()) {
                            final ResourceLocation diffuseLocation = materialDefinition.getDiffuseTexture().orElse(null);

                            if (diffuseLocation != null) {

                                if (diffuseLocation.getResourcePath().endsWith(".png")) {
                                    diffuseSprite =
                                            this.bakedTextureGetter.apply(new ResourceLocation(diffuseLocation.getResourceDomain(), diffuseLocation
                                                    .getResourcePath().split("\\.")[0]));
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

                for (final Face face : group.getFaces()) {
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

                    this.quads.add(quadBuilder.build());
                }
            }

            if (particleFace != null && particleAtlasSprite != null) {
                // For now, last face = particle generation
                this.particleDiffuseSprite = this.createParticleSpriteFor(particleFace, particleAtlasSprite);
            }
        }

        return this.quads;
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
