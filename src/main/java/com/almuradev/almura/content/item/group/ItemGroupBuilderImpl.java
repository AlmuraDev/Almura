/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.group;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.content.item.group.impl.GenericCreativeTab;
import org.spongepowered.api.item.inventory.ItemStack;

public class ItemGroupBuilderImpl implements ItemGroup.Builder {

    private ItemStack itemStack;

    public ItemGroupBuilderImpl() {
        this.reset();
    }

    @Override
    public ItemGroup build(String id, String name) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");
        checkNotNull(name);
        checkState(!name.isEmpty(), "Name cannot be empty!");
        checkNotNull(this.itemStack != null, "Tab icon cannot be null!");

        final ItemStack tabIconStack = this.itemStack;

        return (ItemGroup) (Object) new GenericCreativeTab(id, (net.minecraft.item.ItemStack) (Object) tabIconStack);
    }

    @Override
    public ItemGroup.Builder tabIcon(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }


    @Override
    public ItemGroup.Builder from(ItemGroup value) {
        this.itemStack = value.getTabIcon();
        return this;
    }

    @Override
    public ItemGroup.Builder reset() {
        this.itemStack = (ItemStack) (Object) net.minecraft.item.ItemStack.EMPTY;
        return this;
    }
}
