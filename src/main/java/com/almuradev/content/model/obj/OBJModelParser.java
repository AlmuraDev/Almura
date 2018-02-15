/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

import com.almuradev.content.model.obj.geometry.Face;
import com.almuradev.content.model.obj.geometry.Group;
import com.almuradev.content.model.obj.geometry.MalformedGeometryException;
import com.almuradev.content.model.obj.geometry.Vertex;
import com.almuradev.content.model.obj.geometry.VertexDefinition;
import com.almuradev.content.model.obj.geometry.VertexNormal;
import com.almuradev.content.model.obj.geometry.VertexTextureCoordinate;
import com.almuradev.content.model.obj.material.MalformedMaterialLibraryException;
import com.almuradev.content.model.obj.material.MaterialDefinition;
import com.almuradev.content.model.obj.material.MaterialLibrary;
import com.almuradev.content.registry.ResourceLocations;
import com.google.inject.assistedinject.Assisted;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class OBJModelParser {

    private final Logger logger;
    private final IResourceManager resourceManager;
    private final ResourceLocation source;
    private final IResource resource;

    @Inject
    private OBJModelParser(final Logger logger, @Assisted final IResourceManager resourceManager, @Assisted final ResourceLocation source, @Assisted final IResource resource) {
        this.logger = logger;
        this.resourceManager = resourceManager;
        this.source = source;
        this.resource = resource;
    }

    public IModel parse() throws Exception {

        final OBJModel.Builder objBuilder = OBJModel.builder();

        try (final InputStream stream = this.resource.getInputStream()) {
            final List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());

            Group.Builder groupBuilder = null;
            String currentGroupName = null;

            for (int i = 0; i < lines.size(); i++) {
                final String line = lines.get(i);

                // Skip comments and newlines
                if (line.startsWith(OBJModelConfig.COMMENT) || line.isEmpty()) {
                    continue;
                }

                final int index = line.indexOf(' ');
                final String[] combinedLineContents = line.split(" ");
                final String lineHeader = line.substring(0, index);
                final String[] lineContents = line.substring(index, line.length()).trim().split(" ");

                switch (lineHeader) {
                    case OBJModelConfig.MATERIAL_LIBRARY:
                        @Nullable final ResourceLocation parent = getParent(this.source);
                        objBuilder.materialLibrary(this.parseMaterialLibrary(ResourceLocations.buildResourceLocationPath(lineContents[0],
                                parent.getResourcePath())));
                        break;
                    case OBJModelConfig.VERTEX:
                        try {
                            final float x = Float.parseFloat(lineContents[0]);
                            final float y = Float.parseFloat(lineContents[1]);
                            final float z = Float.parseFloat(lineContents[2]);

                            float w = 1f;

                            if (lineContents.length == 4) {
                                w = Float.parseFloat(lineContents[3]);
                            }

                            objBuilder.vertex(new Vertex(x, y, z, w));
                        } catch (final Exception e) {
                            throw new MalformedGeometryException("Illegal vertex encountered! Vertices must have at least three float values. "
                                    + "Source -> Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents), e);
                        }

                        break;
                    case OBJModelConfig.VERTEX_NORMAL:
                        try {
                            final float x = Float.parseFloat(lineContents[0]);
                            final float y = Float.parseFloat(lineContents[1]);
                            final float z = Float.parseFloat(lineContents[2]);

                            objBuilder.normal(new VertexNormal(x, y, z));
                        } catch (final Exception e) {
                            throw new MalformedGeometryException("Illegal vertex normal encountered! Vertex normals must have at least three float "
                                    + "values. Source -> Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents), e);
                        }

                        break;
                    case OBJModelConfig.VERTEX_TEXTURE_COORDINATE:
                        try {
                            final float u = Math.max(0f, Math.min(1f, Float.parseFloat(lineContents[0])));
                            final float v = Math.max(0f, Math.min(1f, Float.parseFloat(lineContents[1])));

                            float w = 1f;

                            if (lineContents.length == 3) {
                                w = Float.parseFloat(lineContents[2]);
                            }

                            objBuilder.textureCoordinate(new VertexTextureCoordinate(u, v, w));
                        } catch (final Exception e) {
                            throw new MalformedGeometryException("Illegal vertex texture coordinate encountered! Vertex texture coordinates must "
                                    + "have at least two float values. Source -> Line: " + (i + 1) + ", Content: " + Arrays.toString
                                    (combinedLineContents), e);
                        }

                        break;
                    case OBJModelConfig.GROUP:
                        if (groupBuilder != null) {
                            objBuilder.group(groupBuilder.build(currentGroupName));
                        }

                        groupBuilder = Group.builder();
                        currentGroupName = lineContents[0];
                        break;
                    case OBJModelConfig.USE_MATERIAL:
                        if (groupBuilder == null) {
                            groupBuilder = Group.builder();
                            currentGroupName = "default";
                        }

                        final MaterialLibrary materialLibrary = objBuilder.materialLibrary();
                        if (materialLibrary == null) {
                            throw new MalformedGeometryException("Encountered material definition but no material library has been specified! Source "
                                    + "-> Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents));
                        }

                        MaterialDefinition materialDefinition = null;

                        for (final MaterialDefinition material : materialLibrary.getMaterialDefinitions()) {
                            if (material.getName().equals(lineContents[0])) {
                                materialDefinition = material;
                            }
                        }

                        if (materialDefinition == null) {
                            throw new MalformedGeometryException("Encountered material definition that does not exist in material library! Source "
                                    + "-> Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents));
                        }

                        groupBuilder.materialDefinition(materialDefinition);
                        break;
                    case OBJModelConfig.FACE:
                        if (groupBuilder == null) {
                            groupBuilder = Group.builder();
                            currentGroupName = "default";
                        }

                        final Face.Builder faceBuilder = Face.builder();

                        for (final String lineSegment : lineContents) {
                            final String[] indices = lineSegment.split("/");
                            final VertexDefinition.Builder vertexDefBuilder = VertexDefinition.builder();

                            try {
                                final int vertexIndex = Integer.parseInt(indices[0]);
                                Vertex vertex = null;

                                for (final Map.Entry<Vertex, Integer> entry : objBuilder.vertices().entrySet()) {
                                    if (entry.getValue() == vertexIndex) {
                                        vertex = entry.getKey();
                                        break;
                                    }
                                }

                                if (vertex == null) {
                                    throw new MalformedGeometryException("Illegal face encountered! Faces must "
                                            + "have at least a vertex index which is an integer, and exist in the vertices above. Source -> Line: "
                                            + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents));
                                }

                                vertexDefBuilder.vertex(vertex);

                                int textureCoordinateIndex = -1;
                                int normalIndex = -1;

                                // 1/
                                if (indices.length > 1) {

                                    // 1//1
                                    if (indices.length == 3 && indices[1].isEmpty()) {
                                        normalIndex = Integer.parseInt(indices[2]);
                                    } else {

                                        // 1/1/1
                                        textureCoordinateIndex = Integer.parseInt(indices[1]);

                                        if (indices.length > 2) {
                                            normalIndex = Integer.parseInt(indices[2]);
                                        }
                                    }
                                }

                                if (textureCoordinateIndex != -1) {
                                    VertexTextureCoordinate textureCoordinate = null;

                                    for (final Map.Entry<VertexTextureCoordinate, Integer> entry : objBuilder.textureCoordinates().entrySet()) {
                                        if (entry.getValue() == textureCoordinateIndex) {
                                            textureCoordinate = entry.getKey();
                                            break;
                                        }
                                    }

                                    if (textureCoordinate == null) {
                                        throw new MalformedGeometryException("Illegal face encountered! The face has a texture coordinate index "
                                                + "which is out of bounds. Source -> Line: " + (i + 1) + ", Content: " + Arrays.toString
                                                (combinedLineContents));
                                    }

                                    vertexDefBuilder.textureCoordinate(textureCoordinate);
                                }

                                if (normalIndex != -1) {
                                    VertexNormal normal = null;

                                    for (final Map.Entry<VertexNormal, Integer> entry : objBuilder.normals().entrySet()) {
                                        if (entry.getValue() == normalIndex) {
                                            normal = entry.getKey();
                                            break;
                                        }
                                    }

                                    if (normal == null) {
                                        throw new MalformedGeometryException("Illegal face encountered! The face has a normal index which is out of"
                                                + " bounds. Source -> Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents));
                                    }

                                    vertexDefBuilder.normal(normal);
                                }
                            } catch (final Exception e) {
                                if (e instanceof MalformedGeometryException) {
                                    throw e;
                                }

                                throw new MalformedGeometryException("Illegal face encountered! All indices in a face must be integers. Source -> "
                                        + "Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents), e);
                            }

                            faceBuilder.vertex(vertexDefBuilder.build());
                        }

                        groupBuilder.face(faceBuilder.build());
                        break;
                    default:
//                        this.logger.debug("Encountered unsupported element [{}] while parsing obj! Source -> "
//                                + "Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents), lineHeader);
                }
            }

            if (groupBuilder != null) {
                objBuilder.group(groupBuilder.build(currentGroupName));
            }
        }

        return objBuilder.build(this.source, getFileName(this.source).split("\\.")[0]);
    }

    private MaterialLibrary parseMaterialLibrary(final ResourceLocation source) throws Exception {
        final MaterialLibrary.Builder mtllibBuilder = MaterialLibrary.builder();

        final IResource resource = this.resourceManager.getResource(source);

        try (final InputStream stream = resource.getInputStream()) {
            MaterialDefinition.Builder mtlBuilder = null;
            String currentMaterial = null;

            final List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
            for (int i = 0; i < lines.size(); i++) {
                final String line = lines.get(i);

                // Skip comments and newlines
                if (line.startsWith(OBJModelConfig.COMMENT) || line.isEmpty()) {
                    continue;
                }

                final String[] combinedLineContents = line.split(" ");
                final String lineHeader = combinedLineContents[0];
                final String[] lineContents = Arrays.copyOfRange(combinedLineContents, 1, combinedLineContents.length);

                switch (lineHeader) {
                    case OBJModelConfig.Material.NEW_MATERIAL:
                        if (mtlBuilder != null) {
                            mtllibBuilder.materialDefinition(mtlBuilder.build(currentMaterial));
                        }

                        mtlBuilder = MaterialDefinition.builder();
                        currentMaterial = lineContents[0];
                        break;
                    case OBJModelConfig.Material.DIFFUSE:
                        if (mtlBuilder == null) {
                            throw new MalformedMaterialLibraryException("Material attribute cannot occur before defining new material "
                                    + "definition! Source -> Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents));
                        }

                        @Nullable final ResourceLocation parentLocation = getParent(this.source);
                        String parentPath = null;

                        if (parentLocation != null) {
                            // Making some assumptions here...carry on now
                            final int lastSlashIndex = parentLocation.getResourcePath().lastIndexOf("/");

                            if (lastSlashIndex != -1) {
                                parentPath = parentLocation.getResourcePath().substring(lastSlashIndex + 1,
                                        parentLocation.getResourcePath().length());
                            }
                        }

                        mtlBuilder.diffuseTexture(ResourceLocations.buildResourceLocationPath(lineContents[0], parentPath));
                        break;
                    default:
//                        this.logger.debug("Encountered unsupported element [{}] while parsing material library! Source -> "
//                                + "Line: " + (i + 1) + ", Content: " + Arrays.toString(combinedLineContents), lineHeader);

                }
            }

            if (mtlBuilder != null) {
                mtllibBuilder.materialDefinition(mtlBuilder.build(currentMaterial));
            }
        }

        return mtllibBuilder.build(source, getFileName(source).split("\\.")[0]);
    }

    /**
     * Gets the parent {@link ResourceLocation} of the provided location or {@link Optional#empty()} otherwise.
     *
     * @param location The location to get the parent of
     * @return The parent
     */
    @Nullable
    public static ResourceLocation getParent(final ResourceLocation location) {
        final String path = location.getResourcePath();

        final int lastSlashIndex = path.lastIndexOf('/');

        if (lastSlashIndex == -1) {
            return null;
        }

        return new ResourceLocation(location.getResourceDomain(), path.substring(0, lastSlashIndex));
    }

    /**
     * Gets the filename in the {@link ResourceLocation}.
     *
     * Note: The returned String may or may not contain the file extension. Resource locations have no contract that
     * enforces an extension so be sure to check.
     *
     * Note: The returned String may not point to a file. Resource locations don't enforce that the path points to
     * an actual file and could very well point to a folder so be sure to check.
     *
     * @param location The location to get the filename of
     * @return The filename
     */
    private static String getFileName(final ResourceLocation location) {
        final String path = location.getResourcePath();
        final int index = path.lastIndexOf('/');
        if (index == ResourceLocations.NOT_FOUND) {
            return path;
        }
        return path.substring(index + 1, path.length());
    }

    public interface Factory {

        OBJModelParser create(final IResourceManager resourceManager, final ResourceLocation source, final IResource resource);
    }
}
