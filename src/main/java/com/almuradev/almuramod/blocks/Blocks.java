/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class Blocks {
    public static final Block EXAMPLE_BLOCK = new AlmuraBlock("exampleBlock", new Material(MapColor.adobeColor), 1f, 1f, 1, true);

    public static void fakeStaticLoad() {}
}
