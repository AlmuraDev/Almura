/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;

public class AlmuraFood extends ItemFood {
    public AlmuraFood(String name, boolean showInCreativeTab, CreativeTabs creativeTabName, int maxStackSize, int healAmount, float saturationModifier, boolean isWolfsFavorite, boolean alwaysEdidble) {
        super(healAmount, saturationModifier, isWolfsFavorite);
        if (showInCreativeTab) {
            setCreativeTab(creativeTabName);
        }
        this.setUnlocalizedName(name);
        setTextureName(AlmuraMod.MOD_ID + ":" + name);
        if (alwaysEdidble) {
            setAlwaysEdible();
        }
        GameRegistry.registerItem(this, name);
    }
}
