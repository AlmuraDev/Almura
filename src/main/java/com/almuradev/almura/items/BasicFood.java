/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items;

import com.almuradev.almura.Almura;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;

public class BasicFood extends ItemFood {

    public BasicFood(String name, boolean showInCreativeTab, CreativeTabs creativeTabName, int maxStackSize, int healAmount, float saturationModifier,
                     boolean isWolfsFavorite, boolean alwaysEdible) {
        super(healAmount, saturationModifier, isWolfsFavorite);
        setUnlocalizedName(name);
        setTextureName(Almura.MOD_ID + ":food/" + name);
        setMaxStackSize(maxStackSize);
        if (alwaysEdible) {
            setAlwaysEdible();
        }
        if (showInCreativeTab) {
            setCreativeTab(creativeTabName);
        }
        GameRegistry.registerItem(this, name);
    }
}
