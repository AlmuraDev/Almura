/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.almuradev.almura.pack.PackUtil;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class PackShape extends Shape {

    private final String name;
    public final boolean useVanillaCollision, useVanillaWireframe, useVanillaBlockBounds;
    public final List<Double> collisionCoordinates, wireframeCoordinates, blockBoundsCoordinates;

    public PackShape(String name, boolean useVanillaCollision, List<Double> collisionCoordinates, boolean useVanillaWireframe,
                     List<Double> wireframeCoordinates, boolean useVanillaBlockBounds, List<Double> blockBoundsCoordinates) {
        this.name = name;
        this.useVanillaCollision = useVanillaCollision;
        this.useVanillaWireframe = useVanillaWireframe;
        this.useVanillaBlockBounds = useVanillaBlockBounds;
        this.collisionCoordinates = collisionCoordinates == null ? Lists.<Double>newArrayList() : collisionCoordinates;
        this.wireframeCoordinates = wireframeCoordinates == null ? Lists.<Double>newArrayList() : wireframeCoordinates;
        this.blockBoundsCoordinates = blockBoundsCoordinates == null ? Lists.<Double>newArrayList() : blockBoundsCoordinates;
    }

    public PackShape(String name, List<Face> faces, boolean useVanillaCollision, List<Double> collisionCoordinates, boolean useVanillaWireframe,
                     List<Double> wireframeCoordinates, boolean useVanillaBlockBounds, List<Double> blockBoundsCoordinates) {
        super(faces);
        this.name = name;
        this.useVanillaCollision = useVanillaCollision;
        this.useVanillaWireframe = useVanillaWireframe;
        this.useVanillaBlockBounds = useVanillaBlockBounds;
        this.collisionCoordinates = collisionCoordinates == null ? Lists.<Double>newArrayList() : collisionCoordinates;
        this.wireframeCoordinates = wireframeCoordinates == null ? Lists.<Double>newArrayList() : wireframeCoordinates;
        this.blockBoundsCoordinates = blockBoundsCoordinates == null ? Lists.<Double>newArrayList() : blockBoundsCoordinates;
    }

    public PackShape(String name, PackShape s, boolean useVanillaCollision, List<Double> collisionCoordinates, boolean useVanillaWireframe,
                     List<Double> wireframeCoordinates, boolean useVanillaBlockBounds, List<Double> blockBoundsCoordinates) {
        super(s);
        this.name = name;
        this.useVanillaCollision = useVanillaCollision;
        this.useVanillaWireframe = useVanillaWireframe;
        this.useVanillaBlockBounds = useVanillaBlockBounds;
        this.collisionCoordinates = collisionCoordinates == null ? Lists.<Double>newArrayList() : collisionCoordinates;
        this.wireframeCoordinates = wireframeCoordinates == null ? Lists.<Double>newArrayList() : wireframeCoordinates;
        this.blockBoundsCoordinates = blockBoundsCoordinates == null ? Lists.<Double>newArrayList() : blockBoundsCoordinates;
    }

    public static PackShape createFromReader(String name, YamlConfiguration reader) throws ConfigurationException {
        final ConfigurationNode boundsNode = reader.getChild("Bounds");
        final boolean useVanillaCollision = boundsNode.getChild("Use-Vanilla-Collision").getBoolean(true);
        final List<Double> collisionCoordinates;
        if (!useVanillaCollision) {
            collisionCoordinates = PackUtil.parseStringToDoubleList(boundsNode.getChild("CollisionBox").getString(""));
        } else {
            collisionCoordinates = null;
        }
        final boolean useVanillaWireframe = boundsNode.getChild("Use-Vanilla-Wireframe").getBoolean(true);
        final List<Double> wireframeCoordinates;
        if (!useVanillaWireframe) {
            wireframeCoordinates = PackUtil.parseStringToDoubleList(boundsNode.getChild("WireframeBox").getString(""));
        } else {
            wireframeCoordinates = null;
        }
        final boolean useVanillaBlockBounds = boundsNode.getChild("Use-Vanilla-Block-Bounds").getBoolean(true);
        final List<Double> blockBoundCoordinates;
        if (!useVanillaBlockBounds) {
            blockBoundCoordinates = PackUtil.parseStringToDoubleList(boundsNode.getChild("BlockBounds").getString(""));
        } else {
            blockBoundCoordinates = null;
        }

        final ConfigurationNode shapesNode = reader.getChild("Shapes");
        final List<Face> faces = new LinkedList<>();

        for (Object obj : shapesNode.getList()) {
            final LinkedHashMap map = (LinkedHashMap) obj;

            final String rawCoordinateString = (String) map.get("Coords");
            final int textureIndex = (Integer) map.get("Texture");

            //Convert String coordinates to vertices
            final List<Vertex> vertices = new LinkedList<>();
            for (String rawCoordinate : rawCoordinateString.substring(0, rawCoordinateString.length() - 1).split("\n")) {
                final List<Double> coordinates = PackUtil.parseStringToDoubleList(rawCoordinate);

                //Convert list of coordinates to vertex
                vertices.add(new Vertex(coordinates.get(0), coordinates.get(1), coordinates.get(2)));
            }
            final RenderParameters params = new RenderParameters();
            params.textureSide.set(ForgeDirection.getOrientation(textureIndex));
            final Face face = new PackFace(textureIndex, vertices);
            face.setStandardUV();
            face.setParameters(params);
            faces.add(face);
        }

        PackShape shape = new PackShape(name, faces, useVanillaCollision, collisionCoordinates, useVanillaWireframe, wireframeCoordinates, useVanillaBlockBounds, blockBoundCoordinates);

        //Handle shapes that don't have at least 4 faces
        if (shape.getFaces().length < 4) {
            shape.applyMatrix();

            final PackShape s = new PackShape(shape.getName(), useVanillaCollision, collisionCoordinates, useVanillaWireframe, wireframeCoordinates, useVanillaBlockBounds, blockBoundCoordinates);
            s.addFaces(shape.getFaces());
            final PackShape
                    scaled =
                    new PackShape(shape.getName(), shape, useVanillaCollision, collisionCoordinates, useVanillaWireframe, wireframeCoordinates, useVanillaBlockBounds, blockBoundCoordinates);
            scaled.scale(-1, 1, -1);
            scaled.applyMatrix();
            //Scaled returns non PackFaces, OOP demands a fix
            for (int i = 0; i < 4 - scaled.getFaces().length; i++) {
                s.addFaces(new PackMirrorFace[]{new PackMirrorFace(((PackFace) s.getFaces()[i]).getTextureId(),
                                                                   scaled.getFaces()[i >= scaled.getFaces().length ? scaled.getFaces().length - 1
                                                                                                                   : i])});
            }
            shape = s;
        }
        shape.storeState();
        return shape;
    }

    public String getName() {
        return name;
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(AxisAlignedBB vanillaBB, World world, int x, int y, int z) {
        if (useVanillaCollision) {
            return vanillaBB;
        }

        return AxisAlignedBB.getBoundingBox(x + collisionCoordinates.get(0), y + collisionCoordinates.get(1), z + collisionCoordinates.get(2),
                                            x + collisionCoordinates.get(3), y + collisionCoordinates.get(4), z + collisionCoordinates.get(5));
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(AxisAlignedBB vanillaBB, World world, int x, int y, int z) {
        if (useVanillaWireframe) {
            return vanillaBB;
        }

        return AxisAlignedBB.getBoundingBox(x + wireframeCoordinates.get(0), y + wireframeCoordinates.get(1), z + wireframeCoordinates.get(2),
                                            x + wireframeCoordinates.get(3), y + wireframeCoordinates.get(4), z + wireframeCoordinates.get(5));
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((PackShape) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "PackShape {name= " + name + "}";
    }
}
