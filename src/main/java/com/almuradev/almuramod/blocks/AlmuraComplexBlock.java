/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.blocks;

import com.almuradev.almuramod.entities.AlmuraTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class AlmuraComplexBlock extends AlmuraBlock {
    private final Class<? extends AlmuraTileEntity> clazz;

    public AlmuraComplexBlock(String name, Material material, CreativeTabs creativeTabName, Class<? extends AlmuraTileEntity> clazz) {
        super(name, material, creativeTabName);
        this.clazz = clazz;
    }

    public AlmuraComplexBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, CreativeTabs creativeTabName, Class<? extends AlmuraTileEntity> clazz) {
        super(name, material, hardness, lightLevel, lightOpacity, creativeTabName);
        this.clazz = clazz;
    }

    public AlmuraComplexBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, String textureName, CreativeTabs creativeTabName, Class<? extends AlmuraTileEntity> clazz) {
        super(name, material, hardness, lightLevel, lightOpacity, textureName, creativeTabName);
        this.clazz = clazz;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
