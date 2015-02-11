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

    private final boolean enableCollision, enableWireframe;
    private final AxisAlignedBB collisionBB, wireframeBB;

    public PackPhysics(boolean enableCollision, boolean enableWireframe, AxisAlignedBB collisionBB, AxisAlignedBB wireframeBB) {
        this.enableCollision = enableCollision;
        this.enableWireframe = enableWireframe;
        this.collisionBB = collisionBB;
        this.wireframeBB = wireframeBB;
    }

    public boolean useVanillaCollision() {
        return enableCollision;
    }

    public boolean useVanillaWireframe() {
        return enableWireframe;
    }

    public AxisAlignedBB getCollision(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (enableCollision || collisionBB == null) {
            return fallback;
        }
        return AxisAlignedBB.getBoundingBox(x + collisionBB.minX, y + collisionBB.minY, z + collisionBB.minZ,
                                            x + collisionBB.maxX, y + collisionBB.maxY, z + collisionBB.maxZ);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getWireframe(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (enableWireframe || wireframeBB == null) {
            return fallback;
        }
        return AxisAlignedBB.getBoundingBox(x + wireframeBB.minX, y + wireframeBB.minY, z + wireframeBB.minZ,
                                            x + wireframeBB.maxX, y + wireframeBB.maxY, z + wireframeBB.maxZ);
    }
}
