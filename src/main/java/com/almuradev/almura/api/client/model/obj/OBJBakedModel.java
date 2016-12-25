package com.almuradev.almura.api.client.model.obj;

import com.almuradev.almura.api.client.model.obj.geometry.Face;
import com.almuradev.almura.api.client.model.obj.geometry.Group;
import com.almuradev.almura.api.client.model.obj.geometry.Vertex;
import com.almuradev.almura.api.client.model.obj.geometry.VertexDefinition;
import com.almuradev.almura.api.client.model.obj.geometry.VertexNormal;
import com.almuradev.almura.api.client.model.obj.geometry.VertexTextureCoordinate;
import com.almuradev.almura.api.client.model.obj.material.MaterialDefinition;
import com.almuradev.almura.mixin.interfaces.IMixinTextureAtlasSprite;
import com.almuradev.almura.util.MathUtil;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

public class OBJBakedModel implements IPerspectiveAwareModel {

    private final OBJModel model;
    private final IModelState state;
    private final VertexFormat format;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private TextureAtlasSprite particleSprite = null;

    public OBJBakedModel(OBJModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.model = model;
        this.state = state;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return MapWrapper.handlePerspective(this, this.state, cameraTransformType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        final List<BakedQuad> quads = new LinkedList<>();

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

            // Instruct the renderer to use the entire texture size
            ((IMixinTextureAtlasSprite) diffuseSprite).setMaxU(1f);
            ((IMixinTextureAtlasSprite) diffuseSprite).setMaxV(1f);

            // The stitcher will upscale a texture to the next power of two...we must prepare to scale the uvs in-case this happens
            final float scaleU = (float) MathUtil.getNextPowerOfTwo(diffuseSprite.getIconWidth()) / (float) diffuseSprite.getIconWidth();
            final float scaleV = (float) MathUtil.getNextPowerOfTwo(diffuseSprite.getIconHeight()) / (float) diffuseSprite.getIconHeight();

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
                                quadBuilder.put(e, vertex.getX(), vertex.getY(), vertex.getZ());
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

                                quadBuilder.put(e, u / scaleU, v / scaleV);
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
            }

            // Calculate particle sprite
            if (particleFace != null) {
                this.particleSprite = new TextureAtlasSprite(diffuseSprite.getIconName());
                final IMixinTextureAtlasSprite mixinSprite = (IMixinTextureAtlasSprite) this.particleSprite;
                this.particleSprite.copyFrom(diffuseSprite);

                int vertexDefNum = 0;

                for (VertexDefinition vertex : particleFace.getVertices()) {
                    final VertexTextureCoordinate textureCoordinate = vertex.getTextureCoordinate().orElse(null);
                    if (textureCoordinate == null) {
                        break;
                    }

                    if (vertexDefNum == 3) {
                        mixinSprite.setMinU(textureCoordinate.getU());
                        mixinSprite.setMinV(1f - textureCoordinate.getV());
                    }

                    if (vertexDefNum == 1) {
                        mixinSprite.setMaxU(textureCoordinate.getU());
                        mixinSprite.setMaxV(1f - textureCoordinate.getV());
                    }

                    vertexDefNum++;
                }
            }
        }

        return quads;
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