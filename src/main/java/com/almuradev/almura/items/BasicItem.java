/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items;

import com.almuradev.almura.Almura;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class BasicItem extends Item {
    public BasicItem(String name) {
        this(name, false, null, 1);
    }

    public BasicItem(String name, boolean showInCreativeTab, CreativeTabs creativeTabName, int maxStackSize) {
        if (showInCreativeTab) {
            setCreativeTab(creativeTabName);
        }

        setUnlocalizedName(name);
        setTextureName(Almura.MOD_ID + ":" + name);
        setMaxStackSize(maxStackSize);
        GameRegistry.registerItem(this, name);
    }
}

