/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.group;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.asm.mixin.interfaces.IMixinCreativeTabs;
import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateItemGroupAttributes;
import com.almuradev.almura.content.item.group.impl.GenericCreativeTabs;
import com.almuradev.almura.content.loader.CatalogDelegate;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;

public class ItemGroupBuilderImpl implements ItemGroup.Builder {

    @Nullable
    private CatalogDelegate<ItemType> itemTypeDelegate;
    private ItemStack itemStack;

    public ItemGroupBuilderImpl() {
        this.reset();
    }

    @Override
    public ItemGroup.Builder tabIcon(CatalogDelegate<ItemType> itemTypeDelegate) {
        // TODO If ItemStack is not empty, throw some exception as they are being stupid lol
        this.itemTypeDelegate = itemTypeDelegate;
        return this;
    }

    @Override
    public ItemGroup.Builder tabIcon(ItemStack itemStack) {
        // TODO If CatalogDelegate is not null, throw some exception as they are being stupid lol
        this.itemStack = itemStack;
        return this;
    }


    @Override
    public ItemGroup.Builder from(ItemGroup value) {
        this.itemStack = value.getTabIcon();
        this.itemTypeDelegate = null;
        return this;
    }

    @Override
    public ItemGroup.Builder reset() {
        this.itemStack = (ItemStack) (Object) net.minecraft.item.ItemStack.EMPTY;
        this.itemTypeDelegate = null;
        return this;
    }

    @Override
    public ItemGroup build(String id, String name) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");
        checkNotNull(name);
        checkState(!name.isEmpty(), "Name cannot be empty!");

        final CreativeTabs creativeTabs = new GenericCreativeTabs(id);
        if (itemTypeDelegate != null) {
            ((IMixinDelegateItemGroupAttributes) creativeTabs).setItemTypeDelegate(this.itemTypeDelegate);
        } else {
            ((IMixinCreativeTabs) creativeTabs).setIconItemStack((net.minecraft.item.ItemStack) (Object) this.itemStack);
        }

        return (ItemGroup) (Object) new GenericCreativeTabs(id);
    }
}
