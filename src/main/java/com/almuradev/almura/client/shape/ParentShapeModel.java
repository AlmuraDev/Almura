/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.shape;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class ParentShapeModel extends AbstractShapeModel<ParentShapeModel, ParentShapeModel.Baked> {

    private final List<Quad> quads;

    private ParentShapeModel(List<Quad> quads) {
        // Parent shapes have no dependencies and know no texture
        super(Collections.emptyList(), Collections.emptyList(), null);
        this.quads = quads;
    }

    @Override
    public Baked bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new Baked();
    }

    List<Quad> getQuads() {
        return this.quads;
    }

    static final class Parser extends AbstractShapeModel.Parser<ParentShapeModel> {

        static final String SECTION_QUADS = "quads";
        static final String SECTION_VERTICES = "vertices";
        static final String KEY_TEXTURE = "texture";
        static final String KEY_NORMALS = "normals";

        private static final Vector2f[] uvCoordinates = new Vector2f[4];

        static {
            /*
                Shape format has no knowledge of its UV coordinates but never fret! we do
                know the vertex order and with that knowledge, the baking step can interpolate
                the uv for the vertex. The order is as follows in the diagram below:

                Quad (Quad)

                (0, 0)                (1, 0)
                ____________________________
                | Third             Second |
                |                          |
                |                          |
                |                          |
                |                          |
                |                          |
                | Fourth            First  |
                |__________________________|
                (0, 1)                (1, 1)
             */
            uvCoordinates[0] = new Vector2f(1f, 1f);
            uvCoordinates[1] = new Vector2f(1f, 0f);
            uvCoordinates[2] = new Vector2f(0f, 0f);
            uvCoordinates[3] = new Vector2f(0f, 1f);
        }

        @Override
        ParentShapeModel parse(IResource resource) throws IOException {
            final ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                    .setDefaultOptions(ConfigurationOptions.defaults())
                    .setSource(() -> new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)))
                    .build();

            final ConfigurationNode rootNode = loader.load();
            final List<Quad> quads = new ArrayList<>();
            for (ConfigurationNode quadNode : rootNode.getNode(SECTION_QUADS).getChildrenList()) {
                final int textureId = quadNode.getNode(KEY_TEXTURE).getInt(0);

                final ConfigurationNode verticesNode = quadNode.getNode(SECTION_VERTICES);
                final Quad.Vertex[] vertices = new Quad.Vertex[4];
                for (int i = 0; i < vertices.length; i++) {
                    final ConfigurationNode vertexNode = verticesNode.getChildrenList().get(i);

                    final Float[] coordinates = new Float[3];
                    for (int c = 0; c < coordinates.length; c++) {
                        coordinates[c] = vertexNode.getChildrenList().get(c).getFloat(0);
                    }

                    final Vector2f uvCoordinate = uvCoordinates[i];
                    vertices[i] = new Quad.Vertex(coordinates[0], coordinates[1], coordinates[2], uvCoordinate.getX(), uvCoordinate.getY());
                }

                final ConfigurationNode normalsNode = quadNode.getNode(KEY_NORMALS);
                Vector3f normal;

                try {
                    final List<Float> normals = normalsNode.getList(TypeToken.of(Float.class));
                    normal = new Vector3f(normals.get(0), normals.get(1), normals.get(2));
                } catch (ObjectMappingException e) {
                    throw new IOException(e);
                }

                quads.add(new Quad(vertices, normal, new Quad.PlaceholderTexture(textureId)));
            }

            return new ParentShapeModel(quads);
        }
    }

    public static final class Baked extends AbstractShapeModel.Baked<ParentShapeModel> {

        Baked() {
            super(null);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return Collections.emptyList();
        }
    }
}
