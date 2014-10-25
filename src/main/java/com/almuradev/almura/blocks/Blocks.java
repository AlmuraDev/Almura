/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.blocks;

import com.almuradev.almura.Tabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class Blocks {

    public static final Block ZERO_WHITE = new BasicBlock("0-white", new Material(MapColor.adobeColor), 1f, 1f, 1, Tabs.BUILDING);

    public static void fakeStaticLoad() {
    }
}
