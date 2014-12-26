/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
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

    public final boolean useVanillaCollision, useVanillaWireframe, useVanillaBlockBounds;
    public final List<Double> collisionCoordinates, wireframeCoordinates, blockBoundsCoordinates;
    private final String name;

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
