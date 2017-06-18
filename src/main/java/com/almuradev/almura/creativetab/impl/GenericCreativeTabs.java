/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.creativetab.impl;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class GenericCreativeTabs extends CreativeTabs {

    private final ItemStack tabItemStack;

    public GenericCreativeTabs(String label, ItemStack tabItemStack) {
        super(label);
        this.tabItemStack = tabItemStack;
    }

    @Override
    public ItemStack getTabIconItem() {
        return this.tabItemStack;
    }
}
