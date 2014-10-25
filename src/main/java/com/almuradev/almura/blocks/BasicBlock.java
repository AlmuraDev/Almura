/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.blocks;

import com.almuradev.almura.Almura;
import com.almuradev.almura.items.BasicItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BasicBlock extends Block {

    /**
     * Create block with zero light
     */
    public BasicBlock(String name, Material material, CreativeTabs creativeTabName) {
        this(name, material, 1f, 0f, 0, name, creativeTabName);
    }

    /**
     * Create block with additional parameters
     */
    public BasicBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, CreativeTabs creativeTabName) {
        this(name, material, hardness, lightLevel, lightOpacity, name, creativeTabName);
    }

    /**
     * Create block with additional parameters and specific texture name
     */
    public BasicBlock(String name, Material material, float hardness, float lightLevel, int lightOpacity, String textureName,
                      CreativeTabs creativeTabName) {
        super(material);
        setBlockName(name);
        setBlockTextureName(Almura.MOD_ID + ":" + textureName);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        if (creativeTabName != null) {
            setCreativeTab(creativeTabName);
        }
        GameRegistry.registerBlock(this, BasicItemBlock.class, name);
    }
}
