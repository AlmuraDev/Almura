/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.group.impl;

import com.almuradev.almura.content.item.group.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class GenericCreativeTabs extends CreativeTabs {

    public GenericCreativeTabs(String label) {
        super(label);
    }

    public GenericCreativeTabs(int index, String label) {
        super(index, label);
    }

    @Override
    public ItemStack getTabIconItem() {
        return (ItemStack) (Object) ((ItemGroup) this).getTabIcon();
    }
}
