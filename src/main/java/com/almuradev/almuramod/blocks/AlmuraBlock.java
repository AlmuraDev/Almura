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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class AlmuraBlock extends Block {
    /**
     * Create generic block with zero light
     */
    public AlmuraBlock(String name, Material material, CreativeTabs creativeTabName) {
        this(name, material, 1f, 0f, 0, name, creativeTabName);
    }

    /**
     * Create block with additional parameters
     */
    public AlmuraBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, CreativeTabs creativeTabName) {
        this(name, material, hardness, lightLevel, lightOpacity, name, creativeTabName);
    }

    /**
     * Create block with additional parameters and specifc texture name
     */
    public AlmuraBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, String textureName, CreativeTabs creativeTabName) {
        super(material);
        setBlockName(name);
        setBlockTextureName(AlmuraMod.MOD_ID.toLowerCase() + ":" + textureName);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        if (creativeTabName != null) {
            setCreativeTab(creativeTabName);
        }
        GameRegistry.registerBlock(this, AlmuraItemBlock.class, name);
    }
}
