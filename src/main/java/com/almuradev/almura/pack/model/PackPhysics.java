/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.almuradev.almura.pack.crop.PackCrops;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public final class PackPhysics {

    private final boolean useVanillaCollision, useVanillaWireframe;
    private final AxisAlignedBB collisionBB, wireframeBB;

    public PackPhysics(boolean useVanillaCollision, boolean useVanillaWireframe, AxisAlignedBB collisionBB, AxisAlignedBB wireframeBB) {
        this.useVanillaCollision = useVanillaCollision;
        this.useVanillaWireframe = useVanillaWireframe;
        this.collisionBB = collisionBB;
        this.wireframeBB = wireframeBB;
    }

    public AxisAlignedBB getCollision(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (useVanillaCollision || collisionBB == null) {
            return fallback;
        }
        AxisAlignedBB def = collisionBB.copy();
        final Block block = world.getBlock(x, y, z);

        if (!(block instanceof PackCrops)) {
            final int metadata = world.getBlockMetadata(x, y, z);

            if (metadata <= 3) {
                def.offset(-0.5, -0.5, -0.5);
                switch (metadata) {
                    case 3:
                        def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, def.maxX * -1, def.maxZ, def.maxY, def.minX * -1);
                        break;
                    case 2:
                        def = AxisAlignedBB.getBoundingBox(def.maxZ * -1, def.minY, def.maxX * -1, def.minZ * -1, def.maxY, def.minX * -1);
                        break;
                    case 0:
                        def = AxisAlignedBB.getBoundingBox(def.minX, def.minY, def.maxZ * -1, def.maxX, def.maxY, def.minZ * -1);
                        break;
                }
                def.offset(0.5, 0.5, 0.5);
            }
        }
        return AxisAlignedBB.getBoundingBox(x + def.minX, y + def.minY, z + def.minZ, x + def.maxX, y + def.maxY, z + def.maxZ);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getWireframe(AxisAlignedBB fallback, World world, int x, int y, int z) {
        if (useVanillaWireframe || wireframeBB == null) {
            return fallback;
        }
        AxisAlignedBB def = wireframeBB.copy();
        final Block block = world.getBlock(x, y, z);

        if (!(block instanceof PackCrops)) {
            final int metadata = world.getBlockMetadata(x, y, z);

            if (metadata <= 3) {
                def.offset(-0.5, -0.5, -0.5);
                switch (metadata) {
                    case 3:
                        def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, def.maxX * -1, def.maxZ, def.maxY, def.minX * -1);
                        break;
                    case 2:
                        def = AxisAlignedBB.getBoundingBox(def.maxZ * -1, def.minY, def.maxX * -1, def.minZ * -1, def.maxY, def.minX * -1);
                        break;
                    case 0:
                        def = AxisAlignedBB.getBoundingBox(def.minX, def.minY, def.maxZ * -1, def.maxX, def.maxY, def.minZ * -1);
                        break;
                }
                def.offset(0.5, 0.5, 0.5);
            }
        }
        return AxisAlignedBB.getBoundingBox(x + def.minX, y + def.minY, z + def.minZ, x + def.maxX, y + def.maxY, z + def.maxZ);
    }
}
