/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.blocks;

import com.almuradev.almuramod.AlmuraMod;
import com.almuradev.almuramod.entities.AlmuraTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class Blocks {
    public static final Block EXAMPLE_BLOCK = new AlmuraBlock("exampleBlock", new Material(MapColor.adobeColor), 1f, 1f, 1, AlmuraMod.act_Building);
    public static final Block COMPLEX_BLOCK = new AlmuraComplexBlock("complexBlock", new Material(MapColor.brownColor), 12, 1f, 1, AlmuraMod.act_Barrels, AlmuraTileEntity.class);

    public static void fakeStaticLoad() {}
}
