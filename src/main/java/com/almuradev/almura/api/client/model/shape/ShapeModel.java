/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.shape;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.IModelState;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

public final class ShapeModel implements IModel {
    private final List<Face> faces;

    public ShapeModel(List<Face> faces) {
        this.faces = faces;
    }

    // Shapes don't depend on other shapes
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    // Shapes do not have any known texture pre-bake
    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return null;
    }

    @Override
    public IModelState getDefaultState() {
        return null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("faces", this.faces)
                .toString();
    }

    public static final class Parser {
        public static final String SHAPES_SECTION = "shapes";
        public static final String COORDS_KEY = "coords";
        public static final String TEXTURE_KEY = "texture";

        private final IResource resource;

        public Parser(IResource resource) {
            this.resource = resource;
        }

        public ShapeModel parse() throws IOException {

            // Queue circus theme
            final ConfigurationLoader<ConfigurationNode> loader = YAMLConfigurationLoader.builder()
                    .setDefaultOptions(ConfigurationOptions.defaults())
                    .setSource(() -> new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)))
                    .build();

            final ConfigurationNode rootNode = loader.load();

            final List<Face> faces = new LinkedList<>();

            for (ConfigurationNode face : rootNode.getNode(SHAPES_SECTION).getChildrenList()) {
                final int textureId = face.getNode(TEXTURE_KEY).getInt(0);
                final List<Vertex> vertices = new LinkedList<>();
                for (String rawVertex : face.getNode(COORDS_KEY).getValue().toString().split("\\n")) {
                    final String[] rawCoordinates = rawVertex.split(" ");
                    if (rawCoordinates.length != 3) {
                        throw new IOException("Invalid vertex [" + rawVertex + "]! Should be three numbers.");
                    }

                    double x, y, z;

                    try {
                        x = Double.parseDouble(rawCoordinates[0]);
                        y = Double.parseDouble(rawCoordinates[1]);
                        z = Double.parseDouble(rawCoordinates[2]);
                    } catch (Exception ex) {
                        throw new IOException("An error occurred parsing values in [" + Arrays.toString(rawCoordinates) + "]. Are they numeric?");
                    }

                    vertices.add(new Vertex(x, y, z));
                }

                faces.add(new Face(textureId, vertices));
            }
            return new ShapeModel(faces);
        }
    }
    /**
     * A baked model is an instance of a model. For those curious.
     */
    public static final class Baked implements IPerspectiveAwareModel {

        private final IModelState state;

        public Baked(IModelState state) {
            this.state = state;
        }
        
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            return null;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return null;
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
            return null;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return null;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return null;
        }
    }

    private static final class Face {

        private final int textureId;
        private final List<Vertex> vertices;

        public Face(int textureId, List<Vertex> vertices) {
            checkNotNull(vertices);
            this.textureId = textureId;
            this.vertices = vertices;
        }

        public Face(int textureId, Vertex... vertices) {
            checkNotNull(vertices);
            this.textureId = textureId;
            this.vertices = new LinkedList<>();
            Collections.addAll(this.vertices, vertices);
        }

        public int getTextureId() {
            return this.textureId;
        }

        public List<Vertex> getVertices() {
            return this.vertices;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("textureId", this.textureId)
                    .add("vertices", this.vertices)
                    .toString();
        }
    }

    private static final class Vertex {

        private final Vector3d position;

        public Vertex(Vector3d position) {
            this.position = position;
        }

        public Vertex(double x, double y, double z) {
            this.position = new Vector3d(x, y, z);
        }

        public Vector3d getPosition() {
            return this.position;
        }

        public double getX() {
            return this.position.getX();
        }

        public double getY() {
            return this.position.getY();
        }

        public double getZ() {
            return this.position.getZ();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("position", this.position)
                    .toString();
        }
    }

    private static final class TextureCoordinate {

        private final int textureId;
        private final int u;
        private final int v;
        private final int length;
        private final int width;

        public TextureCoordinate(int textureId, int u, int v, int length, int width) {
            this.textureId = textureId;
            this.u = u;
            this.v = v;
            this.length = length;
            this.width = width;
        }

        public int getTextureId() {
            return u;
        }

        public int getU() {
            return u;
        }

        public int getV() {
            return v;
        }

        public int getLength() {
            return length;
        }

        public int getWidth() {
            return width;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("textureId", this.textureId)
                    .add("u", this.u)
                    .add("v", this.v)
                    .add("length", this.length)
                    .add("width", this.width)
                    .toString();
        }
    }
}
