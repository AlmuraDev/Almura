/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class PackShape extends Shape {

    private final String name;

    public PackShape(String name) {
        this.name = name;
    }

    public PackShape(String name, Face... faces) {
        super(faces);
        this.name = name;
    }

    public PackShape(String name, List<Face> faces) {
        super(faces);
        this.name = name;
    }

    public PackShape(String name, PackShape s) {
        super(s);
        this.name = name;
    }

    public static PackShape createFromReader(String name, YamlConfiguration reader) throws ConfigurationException {
        //Shapes:
        final ConfigurationNode shapesNode = reader.getChild("Shapes");
        final List<Face> faces = new LinkedList<>();

        for (Object obj : shapesNode.getList()) {
            final LinkedHashMap map = (LinkedHashMap) obj;
            final String rawCoordinateString = (String) map.get("Coords");
            final int textureIndex = (Integer) map.get("Texture");

            String[] rawCoordinates = rawCoordinateString.substring(0, rawCoordinateString.length() - 1).split("\n");

            //Convert String coordinates to vertices
            final List<Vertex> vertices = new LinkedList<>();
            for (String rawCoordinate : rawCoordinates) {
                final String[] splitCoordinates = rawCoordinate.split(" ");
                final List<Double> parsedCoordinates = new LinkedList<>();

                for (String coordinate : splitCoordinates) {
                    parsedCoordinates.add(Double.parseDouble(coordinate));
                }

                //Convert list of coordinates to vertex
                vertices.add(new Vertex(parsedCoordinates.get(0), parsedCoordinates.get(1), parsedCoordinates.get(2)));
            }
            final RenderParameters params = new RenderParameters();
            params.textureSide.set(ForgeDirection.getOrientation(textureIndex));
            final Face face = new PackFace(textureIndex, vertices);
            face.setStandardUV();
            face.setParameters(params);
            faces.add(face);
        }

        final PackShape shape = new PackShape(name, faces);
        shape.storeState();
        return shape;
    }

    public String getName() {
        return name;
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
