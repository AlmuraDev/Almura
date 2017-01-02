/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.obj;

import com.almuradev.almura.client.model.obj.geometry.Face;
import com.almuradev.almura.client.model.obj.geometry.Group;
import com.almuradev.almura.client.model.obj.geometry.Vertex;
import com.almuradev.almura.client.model.obj.geometry.VertexDefinition;
import com.almuradev.almura.client.model.obj.geometry.VertexNormal;
import com.almuradev.almura.client.model.obj.geometry.VertexTextureCoordinate;
import com.almuradev.almura.client.model.obj.material.MaterialDefinition;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

public class OBJBakedModel implements IPerspectiveAwareModel {

    private final OBJModel model;
    private final IModelState state;
    private final VertexFormat format;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private TextureAtlasSprite particleSprite = null;
    private List<BakedQuad> quads;

    public OBJBakedModel(OBJModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.model = model;
        this.state = state;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;

        // TODO Figure out how to do particle texture as we do some atlases.
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return MapWrapper.handlePerspective(this, this.state, cameraTransformType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

        if (this.quads == null) {
            this.quads = new LinkedList<>();

            final TRSRTransformation transformation = this.state.apply(com.google.common.base.Optional.absent()).orNull();

            for (Group group : this.model.getGroups()) {
                final MaterialDefinition materialDefinition = group.getMaterialDefinition().orElse(null);

                TextureAtlasSprite diffuseSprite = null;
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

                if (diffuseSprite == null) {
                    diffuseSprite = ModelLoader.White.INSTANCE;
                }

                Face particleFace = null;

                for (Face face : group.getFaces()) {
                    if (particleFace == null) {
                        particleFace = face;
                    }
                    final UnpackedBakedQuad.Builder quadBuilder = new UnpackedBakedQuad.Builder(this.format);
                    quadBuilder.setContractUVs(true);
                    quadBuilder.setTexture(diffuseSprite);
                    VertexNormal normal = null;

                    for (VertexDefinition vertexDef : face.getVertices()) {
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
                                    final VertexTextureCoordinate textureCoordinate = vertexDef.getTextureCoordinate().orElse(null);

                                    float u, v;

                                    if (textureCoordinate != null) {
                                        u = textureCoordinate.getU();
                                        v = 1f - textureCoordinate.getV();
                                    } else {
                                        u = 0f;
                                        v = 1f;
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
        }

        return this.quads;
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
        return this.particleSprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}