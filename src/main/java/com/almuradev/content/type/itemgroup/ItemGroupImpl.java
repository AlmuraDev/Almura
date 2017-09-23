/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.content.type.item.definition.ItemDefinition;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public final class ItemGroupImpl extends CreativeTabs {

    @Nullable private final String translation;
    private final ItemDefinition icon;

    ItemGroupImpl(final String id, @Nullable final String translation, final ItemDefinition icon) {
        super(id);
        this.translation = translation;
        this.icon = icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel() {
        return this.translation != null ? "itemGroup.".concat(this.translation) : super.getTranslatedTabLabel();
    }

    @Override
    public ItemStack getTabIconItem() {
        return this.icon.create();
    }
}
