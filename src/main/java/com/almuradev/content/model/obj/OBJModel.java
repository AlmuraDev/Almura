/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.content.model.obj.geometry.Group;
import com.almuradev.content.model.obj.geometry.Vertex;
import com.almuradev.content.model.obj.geometry.VertexNormal;
import com.almuradev.content.model.obj.geometry.VertexTextureCoordinate;
import com.almuradev.content.model.obj.material.MaterialDefinition;
import com.almuradev.content.model.obj.material.MaterialLibrary;
import com.almuradev.content.registry.ResourceLocations;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * While MinecraftForge does have an OBJLoader and OBJModel, this approach doesn't require changes on the modeller's part when exporting from
 * Blender, 3ds Max, etc and instead dynamically converts the model piped into a format Minecraft understands.
 */
@SideOnly(Side.CLIENT)
public class OBJModel implements IModel {
    private final ResourceLocation source;
    private final String name;
    private final MaterialLibrary materialLibrary;
    private final List<Group> groups;
    private final Set<ResourceLocation> textures;

    private OBJModel(final ResourceLocation source, final String name, final MaterialLibrary materialLibrary, final List<Group> groups) {
        this.source = source;
        this.name = name;
        this.materialLibrary = materialLibrary;
        this.groups = groups;
        this.textures = new HashSet<>();

        for (final MaterialDefinition materialDefinition : materialLibrary.getMaterialDefinitions()) {

            final ResourceLocation diffuseLocation = materialDefinition.getDiffuseTexture().orElse(null);

            if (diffuseLocation == null) {
                continue;
            }

            if (diffuseLocation.getPath().endsWith(".png")) {
                this.textures.add(new ResourceLocation(diffuseLocation.getNamespace(), diffuseLocation
                        .getPath().split("\\.")[0]));
            } else {
                this.textures.add(diffuseLocation);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public IModel process(final ImmutableMap<String, String> customData) {
        return new OBJModel(this.source, this.name, this.materialLibrary, this.groups);
    }

    @Override
    public IModel retexture(final ImmutableMap<String, String> textures) {
        final MaterialLibrary.Builder matLibBuilder = MaterialLibrary.builder().from(this.materialLibrary);

        for (final Map.Entry<String, String> textureEntry : textures.entrySet()) {
            final String rawMaterialDefinition = textureEntry.getKey();
            final String rawResourceLocation = textureEntry.getValue();

            // Texture replacements must start with # for the material definition
            if (!rawMaterialDefinition.startsWith("#")) {
                // TODO log this
                continue;
            } else {
                final String materialDefinition = rawMaterialDefinition.substring(1, rawMaterialDefinition.length());
                MaterialDefinition.Builder matDefBuilder = null;

                final Iterator<MaterialDefinition> iterator = matLibBuilder.materialDefinitions().iterator();
                while (iterator.hasNext()) {
                    final MaterialDefinition current = iterator.next();
                    if (current.getName().equalsIgnoreCase(materialDefinition)) {
                        matDefBuilder = MaterialDefinition.builder().from(current);
                        iterator.remove();
                        break;
                    }
                }

                if (matDefBuilder == null) {
                    // TODO log this
                    continue;
                }

                // TODO Need to figure out if we have a parent
                matDefBuilder.diffuseTexture(ResourceLocations.buildResourceLocationPath(rawResourceLocation, null));

                matLibBuilder.materialDefinition(matDefBuilder.build(materialDefinition));
            }
        }

        return new OBJModel(this.source, this.name, matLibBuilder.build(this.materialLibrary.getSource(), this.materialLibrary.getName()), this.groups);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return this.textures;
    }

    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new OBJBakedModel(this, state, format, bakedTextureGetter);
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getSource() {
        return this.source;
    }

    public MaterialLibrary getMaterialLibrary() {
        return this.materialLibrary;
    }

    public List<Group> getGroups() {
        return this.groups;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("source", this.source)
                .add("groups", this.groups)
                .toString();
    }

    public static final class Builder {
        private MaterialLibrary materialLibrary;
        private LinkedHashMap<Vertex, Integer> vertices = new LinkedHashMap<>();
        private LinkedHashMap<VertexNormal, Integer> normals = new LinkedHashMap<>();
        private LinkedHashMap<VertexTextureCoordinate, Integer> textureCoordinates = new LinkedHashMap<>();
        private List<Group> groups = new ArrayList<>();

        public Builder materialLibrary(final MaterialLibrary materialLibrary) {
            this.materialLibrary = materialLibrary;
            return this;
        }

        public MaterialLibrary materialLibrary() {
            return this.materialLibrary;
        }

        public Builder vertex(final Vertex vertex) {
            final int index = this.vertices.size() + 1;
            this.vertices.put(vertex, index);
            vertex.setIndex(index);
            return this;
        }

        public LinkedHashMap<Vertex, Integer> vertices() {
            return this.vertices;
        }

        public Builder normal(final VertexNormal normal) {
            final int index = this.normals.size() + 1;
            this.normals.put(normal, index);
            normal.setIndex(index);
            return this;
        }

        public LinkedHashMap<VertexNormal, Integer> normals() {
            return this.normals;
        }

        public Builder textureCoordinate(final VertexTextureCoordinate textureCoordinate) {
            final int index = this.textureCoordinates.size() + 1;
            this.textureCoordinates.put(textureCoordinate, index);
            textureCoordinate.setIndex(index);
            return this;
        }

        public LinkedHashMap<VertexTextureCoordinate, Integer> textureCoordinates() {
            return this.textureCoordinates;
        }

        public Builder group(final Group group) {
            this.groups.add(group);
            return this;
        }

        public OBJModel build(final ResourceLocation source, final String name) {
            checkState(source != null, "Source cannot be null!");
            checkState(name != null, "Name cannot be null!");
            checkState(!name.isEmpty(), "Name cannot be empty!");
            checkState(!this.groups.isEmpty(), "Groups cannot be empty!");

            return new OBJModel(source, name, this.materialLibrary, this.groups);
        }
    }
}
