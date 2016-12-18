/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.shape;

import com.almuradev.almura.client.model.TransformPart;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

public class ParentShapeModel extends AbstractShapeModel<ParentShapeModel, ParentShapeModel.Baked> {

    private final Map<ItemCameraTransforms.TransformType, Map<TransformPart, Vector3f>> perspectives;
    private final List<Quad> quads;

    private ParentShapeModel(Map<ItemCameraTransforms.TransformType, Map<TransformPart, Vector3f>> perspectives, List<Quad> quads) {
        // Parent shapes have no dependencies and know no texture
        super(Collections.emptyList(), Collections.emptyList(), null);
        this.perspectives = perspectives;
        this.quads = quads;
    }

    @Override
    public Baked bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new Baked();
    }

    List<Quad> getQuads() {
        return this.quads;
    }

    Map<ItemCameraTransforms.TransformType, Map<TransformPart, Vector3f>> getPerspectives() {
        return this.perspectives;
    }

    static final class Parser extends AbstractShapeModel.Parser<ParentShapeModel> {

        static final String SECTION_PERSPECTIVE = "perspective";
        static final String SECTION_QUADS = "quads";
        static final String SECTION_VERTICES = "vertices";
        static final String KEY_TEXTURE = "texture";
        static final String KEY_NORMALS = "normals";
        static final String KEY_COLOR = "color";

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
        ParentShapeModel parse(ResourceLocation source, IResource resource) throws IOException, ObjectMappingException {
            final ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                    .setDefaultOptions(ConfigurationOptions.defaults())
                    .setSource(() -> new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)))
                    .build();

            final ConfigurationNode rootNode = loader.load();

            final Map<ItemCameraTransforms.TransformType, Map<TransformPart, Vector3f>> perspective = new HashMap<>();

            final ConfigurationNode perspectiveNode = rootNode.getNode(SECTION_PERSPECTIVE);
            for (ItemCameraTransforms.TransformType type : ItemCameraTransforms.TransformType.values()) {
                // TODO Maybe expose HEAD transform type?
                if (type == ItemCameraTransforms.TransformType.NONE || type == ItemCameraTransforms.TransformType.HEAD) {
                    continue;
                }
                final String typeKey = type.name().toLowerCase(Locale.ENGLISH);

                final ConfigurationNode typeNode = perspectiveNode.getNode(typeKey);
                final Map<TransformPart, Vector3f> adjustmentsByPart = new HashMap<>();

                for (TransformPart part : TransformPart.values()) {
                    final String partKey = part.name().toLowerCase(Locale.ENGLISH);

                    final ConfigurationNode partNode = typeNode.getNode(partKey);

                    final List<Float> coordinate = partNode.getList(TypeToken.of(Float.class));
                    if (coordinate.size() != 3) {
                        throw new IOException("Attempt to parse parent shape [" + source.getResourcePath() + "] failed because transform part [" +
                                partKey + "] under transform type [" + typeKey + "] does not have three points (x, y, z).");
                    }

                    float x = coordinate.get(0);
                    float y = coordinate.get(1);
                    float z = coordinate.get(2);

                    if (x != 0 || y != 0 || z != 0) {
                        adjustmentsByPart.put(part, new Vector3f(x, y, z));
                    }
                }

                perspective.put(type, adjustmentsByPart);
            }

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

                final ConfigurationNode colorNode = quadNode.getNode(KEY_COLOR);
                Vector4f color;
                try {
                    final List<Float> colors = colorNode.getList(TypeToken.of(Float.class));
                    color = new Vector4f(colors.get(0), colors.get(1), colors.get(2), colors.get(3));
                } catch (ObjectMappingException e) {
                    throw new IOException(e);
                }
                quads.add(new Quad(vertices, normal, color, new Quad.PlaceholderTexture(textureId)));
            }

            return new ParentShapeModel(perspective, quads);
        }
    }

    static final class Baked extends AbstractShapeModel.Baked<ParentShapeModel> {

        Baked() {
            super(null);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return Collections.emptyList();
        }
    }
}
