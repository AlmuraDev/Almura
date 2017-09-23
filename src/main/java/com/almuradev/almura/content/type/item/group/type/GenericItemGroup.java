/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.group.type;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class GenericItemGroup extends CreativeTabs {

    @Nullable private final String translation;

    public GenericItemGroup(String label, @Nullable final String translation) {
        super(label);
        this.translation = translation;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel() {
        return this.translation != null ? "itemGroup.".concat(this.translation) : super.getTranslatedTabLabel();
    }

    @Override
    public ItemStack getTabIconItem() {
        return (ItemStack) (Object) ((ItemGroup) this).getIcon();
    }
}