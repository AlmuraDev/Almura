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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public final class ItemGroupImpl extends CreativeTabs {
    @Nullable private final String translation;
    private final ItemDefinition icon;
    private final boolean sort;

    ItemGroupImpl(final String id, @Nullable final String translation, final ItemDefinition icon, final boolean sort) {
        super(id);
        this.translation = translation;
        this.icon = icon;
        this.sort = sort;
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

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void displayAllRelevantItems(final NonNullList<ItemStack> items) {
        super.displayAllRelevantItems(items);
        if (this.sort) {
            items.sort((o1, o2) -> {
                final Item i1 = o1.getItem();
                final Item i2 = o2.getItem();
                final ResourceLocation id1 = i1 instanceof ItemBlock ? ((ItemBlock) i1).getBlock().getRegistryName() : i1.getRegistryName();
                final ResourceLocation id2 = i2 instanceof ItemBlock ? ((ItemBlock) i2).getBlock().getRegistryName() : i2.getRegistryName();
                return id1.compareTo(id2);
            });
        }
    }
}
