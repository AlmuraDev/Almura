/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.obj;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.api.client.model.obj.geometry.Group;
import com.almuradev.almura.api.client.model.obj.geometry.Vertex;
import com.almuradev.almura.api.client.model.obj.geometry.VertexNormal;
import com.almuradev.almura.api.client.model.obj.geometry.VertexTextureCoordinate;
import com.almuradev.almura.api.client.model.obj.material.MaterialLibrary;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelCustomData;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.model.IModelState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

/**
 * While MinecraftForge does have an OBJLoader and OBJModel, this approach doesn't require changes on the modeller's part when exporting from
 * Blender, 3ds Max, etc and instead dynamically converts the model piped into a format Minecraft understands.
 */
public class OBJModel implements IRetexturableModel, IModelCustomData {

    private final ResourceLocation source;
    private final String name;
    private final MaterialLibrary library;
    private final LinkedHashMap<Vertex, Integer> vertices;
    private final LinkedHashMap<VertexNormal, Integer> normals;
    private final LinkedHashMap<VertexTextureCoordinate, Integer> textureCoordinates;
    private final Set<Group> groups;

    private OBJModel(ResourceLocation source, String name, MaterialLibrary library, LinkedHashMap<Vertex, Integer> vertices,
            LinkedHashMap<VertexNormal, Integer> normals, LinkedHashMap<VertexTextureCoordinate, Integer> textureCoordinates, Set<Group> groups) {
        this.source = source;
        this.name = name;
        this.library = library;
        this.vertices = vertices;
        this.normals = normals;
        this.textureCoordinates = textureCoordinates;
        this.groups = groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        return null;
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        return null;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return null;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return null;
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
                .add("name", this.name)
                .add("source", this.source)
                .add("vertices", this.vertices)
                .add("normals", this.normals)
                .add("textureCoordinates", this.textureCoordinates)
                .add("groups", this.groups)
                .toString();
    }

    public static final class Baked implements IPerspectiveAwareModel {

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
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
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

    public static final class Builder {

        private MaterialLibrary materialLibrary;
        private LinkedHashMap<Vertex, Integer> vertices = new LinkedHashMap<>();
        private LinkedHashMap<VertexNormal, Integer> normals = new LinkedHashMap<>();
        private LinkedHashMap<VertexTextureCoordinate, Integer> textureCoordinates = new LinkedHashMap<>();
        private Set<Group> groups = new HashSet<>();

        public Builder materialLibrary(MaterialLibrary materialLibrary) {
            this.materialLibrary = materialLibrary;
            return this;
        }

        public MaterialLibrary materialLibrary() {
            return this.materialLibrary;
        }

        public Builder vertex(Vertex vertex) {
            final int index = this.vertices.size() + 1;
            this.vertices.put(vertex, index);
            vertex.setIndex(index);
            return this;
        }

        public LinkedHashMap<Vertex, Integer> vertices() {
            return this.vertices;
        }

        public Builder normal(VertexNormal normal) {
            final int index = this.normals.size() + 1;
            this.normals.put(normal, index);
            normal.setIndex(index);
            return this;
        }

        public LinkedHashMap<VertexNormal, Integer> normals() {
            return this.normals;
        }

        public Builder textureCoordinate(VertexTextureCoordinate textureCoordinate) {
            final int index = this.textureCoordinates.size() + 1;
            this.textureCoordinates.put(textureCoordinate, index);
            textureCoordinate.setIndex(index);
            return this;
        }

        public LinkedHashMap<VertexTextureCoordinate, Integer> textureCoordinates() {
            return this.textureCoordinates;
        }

        public Builder group(Group group) {
            this.groups.add(group);
            return this;
        }

        public OBJModel build(ResourceLocation source, String name) {
            checkState(source != null, "Source cannot be null!");
            checkState(name != null, "Name cannot be null!");
            checkState(!name.isEmpty(), "Name cannot be empty!");
            checkState(!this.vertices.isEmpty(), "Vertices cannot be empty!");
            checkState(!this.groups.isEmpty(), "Groups cannot be empty!");

            return new OBJModel(source, name, this.materialLibrary, this.vertices, this.normals, this.textureCoordinates, this.groups);
        }
    }
}
