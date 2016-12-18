/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.shape;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.IModelState;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Collection;

import javax.vecmath.Matrix4f;

abstract class AbstractShapeModel<S extends AbstractShapeModel, B extends AbstractShapeModel.Baked<S>> implements IModel {

    private final Collection<ResourceLocation> dependencies, textures;
    private final IModelState defaultModelState;

    AbstractShapeModel(Collection<ResourceLocation> dependencies, Collection<ResourceLocation> textures, IModelState defaultModelState) {
        this.dependencies = dependencies;
        this.textures = textures;
        this.defaultModelState = defaultModelState;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return this.textures;
    }

    @Override
    public abstract B bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter);

    @Override
    public IModelState getDefaultState() {
        return this.defaultModelState;
    }

    abstract static class Parser<S extends AbstractShapeModel<S, ? extends Baked<S>>> {

        static final ResourceLocation LOCATION_MISSING_TEXTURE = ModelLoader.White.LOCATION;

        abstract S parse(ResourceLocation source, IResource resource) throws IOException, ObjectMappingException;
    }

    abstract static class Baked<S extends AbstractShapeModel> implements IPerspectiveAwareModel {

        static final TextureAtlasSprite SPRITE_MISSING = ModelLoader.White.INSTANCE;
        final S cookableModel;
        private final Pair<Baked, Matrix4f> defaultPerspective;

        Baked(S cookableModel) {
            this.cookableModel = cookableModel;
            this.defaultPerspective = Pair.of(this, null);
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

        /**
         * Particle texture is only known to the child. Here is our fallback.
         *
         * @return Particle texture
         */
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return SPRITE_MISSING;
        }

        /**
         * Deprecated code replaced by {@link IPerspectiveAwareModel#handlePerspective(ItemCameraTransforms.TransformType)}.
         *
         * Forge hasn't removed yet so pass default...
         *
         * @return ignore
         */
        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        /**
         * Shapes do not change their model as an item. Here is our fallback.
         *
         * @return No item overrides
         */
        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        /**
         * Perspective is handled by the parent. By default, no perspective changes.
         *
         * @param cameraTransformType The transform type of the perspective
         * @return The perspective adjustment
         */
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            return this.defaultPerspective;
        }
    }

    /*
     * Utility classes
     */

    static final class Quad {

        final Vertex[] vertices;
        final Vector3f normal;
        final Vector4f color;
        protected Texture texture;

        Quad(Vertex[] vertices, Vector3f normal, Vector4f color) {
            checkNotNull(vertices);
            this.vertices = vertices;
            this.normal = normal;
            this.color = color;
        }

        Quad(Vertex[] vertices, Vector3f normal, Vector4f color, Texture texture) {
            this(vertices, normal, color);
            this.texture = texture;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("texture", this.texture)
                    .add("vertices", this.vertices)
                    .add("normal", this.normal)
                    .add("color", this.color)
                    .toString();
        }

        static final class Vertex {

            final float x, y, z, u, v;
            final Vector3f vector;

            Vertex(float x, float y, float z, float u, float v) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.u = u;
                this.v = v;

                this.vector = new Vector3f(x, y, z);
            }

            @Override
            public String toString() {
                return Objects.toStringHelper(this)
                        .add("x", this.x)
                        .add("y", this.y)
                        .add("z", this.z)
                        .add("u", this.u)
                        .add("v", this.v)
                        .toString();
            }
        }

        static class Texture {

            protected final int x;
            protected final int y;
            final ResourceLocation location;
            final int width;
            final int height;

            Texture(ResourceLocation location, int x, int y, int width, int height) {
                this.location = location;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }

            @Override
            public String toString() {
                return Objects.toStringHelper(this)
                        .add("location", this.location)
                        .add("x", this.x)
                        .add("y", this.y)
                        .add("width", this.width)
                        .add("height", this.height)
                        .toString();
            }
        }

        static final class PlaceholderTexture extends Texture {

            final int textureId;

            PlaceholderTexture(int textureId) {
                super(Parser.LOCATION_MISSING_TEXTURE, 0, 0, 1, 1);
                this.textureId = textureId;
            }
        }
    }
}
