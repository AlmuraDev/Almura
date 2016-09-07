/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.tabs;

import com.almuradev.almura.Almura;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IPackObject;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlmuraCreativeTabs extends CreativeTabs {

    private final Item displayItem;

    public AlmuraCreativeTabs(String unlocalizedName, String displayName, Item displayItem) {
        super(Almura.MOD_ID + "_" + unlocalizedName);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "itemGroup." + tabLabel, displayName);
        this.displayItem = displayItem;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return displayItem == null ? Items.feather : displayItem;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void displayAllReleventItems(List list) {
        super.displayAllReleventItems(list);
        Collections.sort(list, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2) {
                final IPackObject object1;
                if (o1.getItem() instanceof ItemBlock) {
                    object1 = (IPackObject) ((ItemBlock) o1.getItem()).blockInstance;
                } else {
                    object1 = (IPackObject) o1.getItem();
                }

                final IPackObject object2;
                if (o2.getItem() instanceof ItemBlock) {
                    object2 = (IPackObject) ((ItemBlock) o2.getItem()).blockInstance;
                } else {
                    object2 = (IPackObject) o2.getItem();
                }
                return object1.getIdentifier().compareTo(object2.getIdentifier());
            }
        });
    }
}
