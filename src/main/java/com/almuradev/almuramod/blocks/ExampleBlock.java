/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.blocks;
import java.util.List;

import com.almuradev.almuramod.AlmuraMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockDirt;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ExampleBlock extends BlockDirt {
    public ExampleBlock() {
        this.setHardness(1.0f);
        this.setLightLevel(2.0f);
        this.setBlockName("exampleBlock");
        this.setCreativeTab(AlmuraMod.CREATIVE_TAB);
    }

    // Override to prevent more than one block since the block we extend adds two
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
        list.add(new ItemStack(this, 1, 0));
    }
}
