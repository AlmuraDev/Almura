/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.group.impl;

import com.almuradev.almura.content.item.group.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class GenericCreativeTabs extends CreativeTabs {

    private final String translation;

    public GenericCreativeTabs(String label, @Nullable final String translation) {
        super(label);
        this.translation = translation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getTranslatedTabLabel() {
        return this.translation != null ? "itemGroup.".concat(this.translation) : super.getTranslatedTabLabel();
    }

    @Override
    public ItemStack getTabIconItem() {
        return (ItemStack) (Object) ((ItemGroup) this).getTabIcon();
    }
}