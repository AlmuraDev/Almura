/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.shape;

import com.almuradev.almura.Almura;
import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

final class ChildShapeModel extends AbstractShapeModel<ChildShapeModel, ChildShapeModel.Baked> {

    private final ResourceLocation parentModelLocation;
    private final List<Face.Texture> textures;

    private ChildShapeModel(ResourceLocation parentModelLocation, Collection<ResourceLocation> textureLocations, List<Face.Texture> textures) {
        super(Lists.newArrayList(parentModelLocation), textureLocations, null);
        this.parentModelLocation = parentModelLocation;
        this.textures = textures;
    }

    @Override
    public Baked bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new Baked(this, format, bakedTextureGetter);
    }

    static final class Parser extends AbstractShapeModel.Parser<ChildShapeModel> {

        static final String SECTION_TEXTURES = "textures";
        static final String KEY_PARENT = "parent";
        static final String KEY_LOCATION = "location";
        static final String KEY_X = "x";
        static final String KEY_Y = "y";
        static final String KEY_WIDTH = "width";
        static final String KEY_HEIGHT = "height";


        @Override
        ChildShapeModel parse(IResource resource) throws IOException {
            final ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                    .setDefaultOptions(ConfigurationOptions.defaults())
                    .setSource(() -> new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)))
                    .build();

            final ConfigurationNode rootNode = loader.load();
            final ResourceLocation parentModelLocation = new ResourceLocation(rootNode.getNode(KEY_PARENT).getString(""));
            final ConfigurationNode texturesNode = rootNode.getNode(SECTION_TEXTURES);
            final Set<ResourceLocation> textureLocations = new HashSet<>();
            final List<Face.Texture> textures = new LinkedList<>();
            for (ConfigurationNode textureNode : texturesNode.getChildrenList()) {
                final ResourceLocation location = new ResourceLocation(textureNode.getNode(KEY_LOCATION).getString(""));
                textureLocations.add(location);
                final int x = textureNode.getNode(KEY_X).getInt(0);
                final int y = textureNode.getNode(KEY_Y).getInt(0);
                final int width = textureNode.getNode(KEY_WIDTH).getInt(0);
                final int height = textureNode.getNode(KEY_HEIGHT).getInt(0);

                textures.add(new Face.Texture(location, x, y, width, height));
            }

            return new ChildShapeModel(parentModelLocation, textureLocations, textures);
        }
    }

    public static final class Baked extends AbstractShapeModel.Baked<ChildShapeModel> {

        final VertexFormat format;
        final Function<ResourceLocation, TextureAtlasSprite> textureGetter;
        ParentShapeModel parentModel;
        List<BakedQuad> quads;
        TextureAtlasSprite particleSprite;

        Baked(ChildShapeModel cookableModel, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
            super(cookableModel);
            this.format = format;
            this.textureGetter = textureGetter;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

            if (parentModel == null) {
                try {
                    parentModel = (ParentShapeModel) ModelLoaderRegistry.getModel(this.cookableModel.parentModelLocation);

                } catch (Exception e) {
                    throw new RuntimeException("Shape [" + this.cookableModel + "] is being baked with no parent specified!");
                }
            }

            if (this.quads == null) {
                final List<BakedQuad> baked = new ArrayList<>();

                for (Face face : parentModel.getFaces()) {
                    final int textureId = ((Face.PlaceholderTexture) face.texture).textureId;

                    Face.Texture texture = this.cookableModel.textures.get(textureId);
                    if (texture == null) {
                        Almura.instance.logger.warn("When baking face [{}] for model [{}], the entry for texture id [{}] was not "
                                + "found! You have a mismatch of textures from your parent [{}]. Using fallback texture instead...", face,
                                this.cookableModel, textureId, parentModel);
                        texture = face.texture;
                    }

                    TextureAtlasSprite sprite = this.textureGetter.apply(texture.location);
                    // Safety fallback in-case texture isn't found
                    if (sprite == null) {
                        Almura.instance.logger.warn("When baking face [{}] for model [{}], the texture for id [{}] with entry [{}] was not found! "
                                        + "Using fallback texture instead...", face, this.cookableModel, textureId, texture);
                        sprite = ModelLoader.White.INSTANCE;
                    }

                    if (particleSprite == null) {
                        final Icon malisisIcon = new Icon(sprite);
                        particleSprite = malisisIcon.clip(texture.x, texture.y, texture.width, texture.height);
                    }

                    final UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(this.format);
                    builder.setTexture(sprite);
                    builder.setQuadOrientation(EnumFacing.WEST);

                    // Calculate normals.
                    // TODO These are planar normals, for barrels or other spherical things I should use interpolated normals. Not a priority at this
                    // TODO moment
                    final Vector3f a = face.vertices[0].vector;
                    final Vector3f b = face.vertices[1].vector;
                    final Vector3f c = face.vertices[2].vector;
                    final Vector3f ac = a.sub(c);
                    final Vector3f bc = b.sub(c);
                    final Vector3f normal = ac.cross(bc).normalize();

                    for (Face.Vertex vertex : face.vertices) {
                        for (int e = 0; e < this.format.getElementCount(); e++) {
                            switch (this.format.getElement(e).getUsage()) {
                                case POSITION:
                                    builder.put(e, vertex.x, vertex.y, vertex.z, 1f);
                                    break;
                                case UV:
                                    // The multiply of 16 is because client divides by 16
                                    float textureU = ((texture.x + (vertex.u * texture.width)) / (float) sprite.getIconWidth()) * 16;
                                    float textureV = ((texture.y + (vertex.v * texture.height)) / (float) sprite.getIconHeight()) * 16;
                                    builder.put(e, sprite.getInterpolatedU(textureU), sprite.getInterpolatedV(textureV), 0, 1);
                                    break;
                                case NORMAL:
                                    builder.put(e, normal.getX(), normal.getY(), normal.getZ(), 1);
                                    break;
                                default:
                                    // Shapes don't define per-vertex coloring so we do black
                                    builder.put(e, 1, 1, 1, 1);
                            }
                        }
                    }

                    baked.add(builder.build());
                }
                this.quads = baked;
            }

            return this.quads;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return this.particleSprite;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            // TODO Cache these transformations as they'll never change

            Matrix4f transformation = null;
            final Matrix4f opMatrix = new Matrix4f();
            switch (cameraTransformType) {
                case FIRST_PERSON_LEFT_HAND:
                case FIRST_PERSON_RIGHT_HAND:
                    transformation = new Matrix4f();
                    transformation.setIdentity();
                    // TODO May need to adjust based on the shape (flags, piano come to mind)
                    transformation.setScale(0.50f);
                    break;
                case THIRD_PERSON_LEFT_HAND:
                case THIRD_PERSON_RIGHT_HAND:
                    transformation = new Matrix4f();
                    transformation.setIdentity();
                    // TODO Quad orientation may make this un-necessary. Investigate.
                    opMatrix.rotX((float) Math.PI / 2.5f);
                    transformation.mul(opMatrix);

                    // TODO May need to adjust based on the shape (flags, piano come to mind)
                    transformation.setScale(0.50f);
                    break;
                case GUI:
                    transformation = new Matrix4f();
                    transformation.setIdentity();
                    // TODO Quad orientation may make this un-necessary. Investigate.
                    opMatrix.rotX((float) Math.PI / 5);
                    transformation.mul(opMatrix);
                    opMatrix.rotY((float) Math.PI / 5);
                    transformation.mul(opMatrix);
                    // TODO Adjust the scale based on 20% of 1 x 1. (Vanilla spec). This needs to be dynamic (as some shapes are quite big)

                    transformation.setScale(0.80f);
                    break;
            }
            return Pair.of(this, transformation);
        }
    }
}
