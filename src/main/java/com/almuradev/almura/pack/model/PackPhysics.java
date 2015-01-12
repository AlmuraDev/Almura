/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public final class PackPhysics {

    private final boolean useVanillaCollision, useVanillaWireframe, useVanillaBlockBounds;
    private final List<Double> collisionCoordinates, wireframeCoordinates, blockBoundsCoordinates;

    public PackPhysics(boolean useVanillaCollision, boolean useVanillaWireframe, boolean useVanillaBlockBounds, List<Double> collisionCoordinates,
                       List<Double> wireframeCoordinates, List<Double> blockBoundsCoordinates) {
        this.useVanillaCollision = useVanillaCollision;
        this.useVanillaWireframe = useVanillaWireframe;
        this.useVanillaBlockBounds = useVanillaBlockBounds;
        this.collisionCoordinates = collisionCoordinates;
        this.wireframeCoordinates = wireframeCoordinates;
        this.blockBoundsCoordinates = blockBoundsCoordinates;
    }

    public boolean useVanillaCollision() {
        return useVanillaCollision;
    }

    public boolean useVanillaWireframe() {
        return useVanillaWireframe;
    }

    public boolean useVanillaBlockBounds() {
        return useVanillaBlockBounds;
    }

    public List<Double> getCollisionCoordinates() {
        return collisionCoordinates;
    }

    public List<Double> getWireframeCoordinates() {
        return wireframeCoordinates;
    }

    public List<Double> getBlockBoundsCoordinates() {
        return blockBoundsCoordinates;
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (useVanillaCollision || collisionCoordinates.size() != 6) {
            return fallback;
        }
        return AxisAlignedBB.getBoundingBox(x + collisionCoordinates.get(0), y + collisionCoordinates.get(1), z + collisionCoordinates.get(2),
                                            x + collisionCoordinates.get(3), y + collisionCoordinates.get(4), z + collisionCoordinates.get(5));
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (useVanillaWireframe || wireframeCoordinates.size() != 6) {
            return fallback;
        }
        return AxisAlignedBB.getBoundingBox(x + wireframeCoordinates.get(0), y + wireframeCoordinates.get(1), z + wireframeCoordinates.get(2),
                                            x + wireframeCoordinates.get(3), y + wireframeCoordinates.get(4), z + wireframeCoordinates.get(5));
    }

    public AxisAlignedBB getBlockBounds(AxisAlignedBB fallback, IBlockAccess access, int x, int y, int z) {
        if (useVanillaBlockBounds || blockBoundsCoordinates.size() != 6) {
            return fallback;
        }

        return AxisAlignedBB.getBoundingBox(blockBoundsCoordinates.get(0), blockBoundsCoordinates.get(1), blockBoundsCoordinates.get(2),
                                            blockBoundsCoordinates.get(3), blockBoundsCoordinates.get(4), blockBoundsCoordinates.get(5));
    }
}
