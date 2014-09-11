/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.blocks;
import java.util.List;

import com.almuradev.almuramod.AlmuraMod;
import com.almuradev.almuramod.items.AlmuraItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AlmuraBlock extends Block {
    public AlmuraBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, boolean showInCreative) {
        super(material);
        setBlockName(name);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        if (showInCreative) {
            setCreativeTab(AlmuraMod.act_Building);
        }
        setBlockTextureName("almuramod:"+name);
        GameRegistry.registerBlock(this, AlmuraItemBlock.class, name);
    }

    // Override to prevent more than one block since the block we extend adds two
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
        list.add(new ItemStack(this, 1, 0));
    }
}
