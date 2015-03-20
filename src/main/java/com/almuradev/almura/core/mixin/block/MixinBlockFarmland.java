/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(value = BlockFarmland.class, remap = false)
public abstract class MixinBlockFarmland extends Block {

    protected MixinBlockFarmland() {
        super(Material.ground);
    }

    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!this.func_149821_m(world, x, y, z) && !world.isRainingAt(x, y + 1, z)) {
            int l = world.getBlockMetadata(x, y, z);

            if (l > 0) {
                world.setBlockMetadataWithNotify(x, y, z, l - 1, 2);
            } else if (!this.func_149822_e(world, x, y, z)) {
                world.setBlock(x, y, z, Blocks.dirt);
            }
        } else {
            world.setBlockMetadataWithNotify(x, y, z, 7, 2);
        }
    }

    private boolean func_149821_m(World world, int x, int y, int z) { //Water or Irrigation Lookup
        for (int l = x - 4; l <= x + 4; ++l) {
            for (int i1 = y; i1 <= y + 1; ++i1) {
                for (int j1 = z - 4; j1 <= z + 4; ++j1) {
                    if (world.getBlock(l, i1, j1).getMaterial() == Material.water) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean func_149822_e(World world, int x, int y, int z) { // Can Plant Crop Here?
        byte b0 = 0;

        for (int l = x - b0; l <= x + b0; ++l) {
            for (int i1 = z - b0; i1 <= z + b0; ++i1) {
                Block block = world.getBlock(l, y + 1, i1);

                if (block instanceof IPlantable && canSustainPlant(world, x, y, z, ForgeDirection.UP, (IPlantable) block)) {
                    return true;
                }
            }
        }

        return false;
    }
}