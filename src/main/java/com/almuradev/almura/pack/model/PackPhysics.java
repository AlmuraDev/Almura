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
        if (!enableCollision || collisionBB == null) {
            return fallback;
        }
        AxisAlignedBB def = collisionBB.copy();
        final int metadata = world.getBlockMetadata(x, y, z);

        if (metadata <= 3) {
            def.offset(-0.5, -0.5, -0.5);
            switch (metadata) {
                // East
                case 3:
                    def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, def.maxX * -1, def.maxZ, def.maxY, def.minX * -1);
                    break;
                // West
                case 2:
                    def = AxisAlignedBB.getBoundingBox(def.maxZ * -1, def.minY, def.maxX * -1, def.minZ * -1, def.maxY, def.minX * -1);
                    break;
                // North
                case 0:
                    def = AxisAlignedBB.getBoundingBox(def.minX, def.minY, def.maxZ * -1, def.maxX, def.maxY, def.minZ * -1);
                    break;
            }
            def.offset(0.5, 0.5, 0.5);
        }

        return AxisAlignedBB.getBoundingBox(x + def.minX, y + def.minY, z + def.minZ, x + def.maxX, y + def.maxY, z + def.maxZ);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getWireframe(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (!enableWireframe || wireframeBB == null) {
            return fallback;
        }
        AxisAlignedBB def = wireframeBB.copy();
        final int metadata = world.getBlockMetadata(x, y, z);

        if (metadata <= 3) {
            def.offset(-0.5, -0.5, -0.5);
            switch (metadata) {
                // East
                case 3:
                    def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, def.maxX * -1, def.maxZ, def.maxY, def.minX * -1);
                    break;
                // West
                case 2:
                    def = AxisAlignedBB.getBoundingBox(def.maxZ * -1, def.minY, def.maxX * -1, def.minZ * -1, def.maxY, def.minX * -1);
                    break;
                // North
                case 0:
                    def = AxisAlignedBB.getBoundingBox(def.minX, def.minY, def.maxZ * -1, def.maxX, def.maxY, def.minZ * -1);
                    break;
            }
            def.offset(0.5, 0.5, 0.5);
        }

        return AxisAlignedBB.getBoundingBox(x + def.minX, y + def.minY, z + def.minZ, x + def.maxX, y + def.maxY, z + def.maxZ);
    }
}
