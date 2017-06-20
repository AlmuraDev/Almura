/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.group.impl;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class GenericCreativeTab extends CreativeTabs {

    private final ItemStack tabItemStack;

    public GenericCreativeTab(String label, ItemStack tabItemStack) {
        super(label);
        this.tabItemStack = tabItemStack;
    }

    @Override
    public ItemStack getTabIconItem() {
        return this.tabItemStack;
    }
}
