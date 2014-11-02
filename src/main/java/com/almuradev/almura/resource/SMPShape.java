/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.resource;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class SMPShape extends Shape {

    private final String name;

    public SMPShape(String name) {
        this.name = name;
    }

    public SMPShape(String name, Face... faces) {
        super(faces);
        this.name = name;
    }

    public SMPShape(String name, List<Face> faces) {
        super(faces);
        this.name = name;
    }

    public SMPShape(String name, Shape s) {
        super(s);
        this.name = name;
    }

    public static SMPShape createFromSMPFile(Path file) throws FileNotFoundException, ConfigurationException {
        if (!file.endsWith(".shape")) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.warn("Attempted to load a shape from file that was not a SHAPE: " + file);
            }
            return null;
        }

        final String fileName = file.toFile().getName().split(".shape")[0];
        return createFromSMPStream(fileName, new FileInputStream(file.toFile()));
    }

    public static SMPShape createFromSMPStream(String name, InputStream stream) throws ConfigurationException {
        final YamlConfiguration reader = new YamlConfiguration(stream);
        reader.load();

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

            faces.add(new SMPFace(textureIndex, vertices));
        }

        return new SMPShape(name, faces);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((SMPShape) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Shape {name= " + name + "}";
    }
}
