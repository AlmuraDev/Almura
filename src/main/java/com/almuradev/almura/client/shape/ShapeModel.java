/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.shape;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
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
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        final List<ResourceLocation> textures = new ArrayList<>();
        textures.add(new ResourceLocation(Almura.PLUGIN_ID, "blocks/flags1"));
        return textures;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new Baked(this, state, format, bakedTextureGetter);
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
                final Vector3d[] vertices = new Vector3d[4];
                int vertNum = 0;
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

                    vertices[vertNum++] = new Vector3d(x, y, z);
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
        /**
            Face (Quad)

            (0, 0)                     (1, 0)
            _________________________________
            | Third                  Second |
            |                               |
            |                               |
            |                               |
            |                               |
            |                               |
            | Fourth                 First  |
            |_______________________________|
            (0, 1)                     (1, 1)
         */
        private static final TextureCoordinate[] textureCoordinates = new TextureCoordinate[4];
        private static final TextureAtlasSprite WHITE = ModelLoader.White.INSTANCE;
        private final ShapeModel model;
        private final IModelState state;
        private final VertexFormat format;
        private final Function<ResourceLocation, TextureAtlasSprite> textureFunction;
        private TextureAtlasSprite sprite;
        private ImmutableList<BakedQuad> quads;

        static {
            // TODO We can skip an array lookup by setting this on the vertex
            textureCoordinates[0] = new TextureCoordinate(1f, 1f);
            textureCoordinates[1] = new TextureCoordinate(1f, 0f);
            textureCoordinates[2] = new TextureCoordinate(0f, 0f);
            textureCoordinates[3] = new TextureCoordinate(0f, 1f);
        }

        public Baked(ShapeModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureFunction) {
            this.model = model;
            this.state = state;
            this.format = format;
            this.textureFunction = textureFunction;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            // We don't build quads based on side
            if (side != null) {
                return ImmutableList.of();
            }

            // TODO Restore quads cache once I am done testing
            //if (this.quads == null) {

            // TODO Testing Code for now. Will need to get this info from the model

//            this.sprite = this.textureFunction.apply(new ResourceLocation(Almura.PLUGIN_ID, "blocks/seedbarrels"));
            this.sprite = this.textureFunction.apply(new ResourceLocation(Almura.PLUGIN_ID, "blocks/flags1"));

            final Map<Integer, FaceTextureEntry> faceTextureEntries = new HashMap<>();

            // texIndex | x | y | height | height
//            faceTextureEntries.put(0, new FaceTextureEntry(0, 0, 0, 128, 128));
//            faceTextureEntries.put(1, new FaceTextureEntry(1, 128, 0, 128, 128));
//            faceTextureEntries.put(2, new FaceTextureEntry(2, 256, 0, 128, 128));
//            faceTextureEntries.put(3, new FaceTextureEntry(3, 384, 0, 128, 128));
//            faceTextureEntries.put(4, new FaceTextureEntry(4, 512, 0, 128, 128));
//            faceTextureEntries.put(5, new FaceTextureEntry(5, 128, 640, 128, 128));

            faceTextureEntries.put(0, new FaceTextureEntry(0, 0, 0, 32, 32));
            faceTextureEntries.put(1, new FaceTextureEntry(1, 32, 0, 32, 32));
            faceTextureEntries.put(2, new FaceTextureEntry(2, 64, 0, 32, 32));
            faceTextureEntries.put(3, new FaceTextureEntry(3, 96, 0, 32, 32));

            final List<BakedQuad> quadsToAdd = new ArrayList<>();

            for (Face face : this.model.faces) {

                final FaceTextureEntry faceTextureEntry = faceTextureEntries.get(face.textureId);

                final UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(this.format);
                builder.setTexture(this.sprite);
                builder.setContractUVs(true);
                builder.setQuadOrientation(EnumFacing.WEST);

                int vertNum = 0;

                // Calculate normals.
                // TODO These are planar normals, for barrels or other spherical things I should use interpolated normals. Not a priority at this
                // TODO moment
                final Vector3d a = face.vertices[0];
                final Vector3d b = face.vertices[1];
                final Vector3d c = face.vertices[2];
                final Vector3d ba = a.sub(c);
                final Vector3d bc = b.sub(c);
                final Vector3d normal =  ba.cross(bc).normalize();

                for (Vector3d vertex : face.vertices) {
                    for (int e = 0; e < this.format.getElementCount(); e++) {
                        switch (this.format.getElement(e).getUsage()) {
                            case POSITION:
                                builder.put(e, (float) vertex.getX(), (float) vertex.getY(), (float) vertex.getZ(), 1f);
                                break;
                            case UV:
                                // The multiply of 16 is because client divides by 16
                                final TextureCoordinate coordinate = textureCoordinates[vertNum++];
                                float u = ((faceTextureEntry.x + (coordinate.u * faceTextureEntry.width)) / (float) this.sprite.getIconWidth()) * 16;
                                float v = ((faceTextureEntry.y + (coordinate.v * faceTextureEntry.height)) / (float) this.sprite.getIconHeight()) *
                                        16;
                                builder.put(e, this.sprite.getInterpolatedU(u), this.sprite.getInterpolatedV(v), 0, 1);
                                break;
                            case NORMAL:
                                builder.put(e, (float) normal.getX(), (float) normal.getY(), (float) normal.getZ(), 1);
                                break;
                            default:
                                // Shapes don't define per-vertex coloring so we do black
                                builder.put(e, 1, 1, 1, 1);
                        }
                    }
                }

                quadsToAdd.add(builder.build());
            }

            this.quads = ImmutableList.copyOf(quadsToAdd);
            // }

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
            return this.sprite == null ? WHITE : this.sprite;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
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

    private static final class Face {

        private final int textureId;
        private final Vector3d[] vertices;

        public Face(int textureId, Vector3d[] vertices) {
            checkNotNull(vertices);
            this.textureId = textureId;
            this.vertices = vertices;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("textureId", this.textureId)
                    .add("vertices", this.vertices)
                    .toString();
        }
    }

    private static final class FaceTextureEntry {

        private final int textureId;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public FaceTextureEntry(int textureId, int x, int y, int width, int height) {
            this.textureId = textureId;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("textureId", this.textureId)
                    .add("x", this.x)
                    .add("y", this.y)
                    .add("width", this.width)
                    .add("height", this.height)
                    .toString();
        }
    }

    private static final class TextureCoordinate {
        private final float u, v;

        public TextureCoordinate(float u, float v) {
            this.u = u;
            this.v = v;
        }
    }
}
