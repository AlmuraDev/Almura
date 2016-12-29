/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.creativetab;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.api.creativetab.CreativeTab;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.item.inventory.ItemStack;

public class CreativeTabBuilderImpl implements CreativeTab.Builder {
    private ItemStack itemStack;

    @Override
    public CreativeTab build(String id, String name) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");
        checkNotNull(name);
        checkState(!name.isEmpty(), "Name cannot be empty!");
        checkNotNull(this.itemStack != null, "Tab icon cannot be null!");
        checkState(!((net.minecraft.item.ItemStack) (Object) this.itemStack).isEmpty(), "Tab icon cannot be empty stack!");

        final ItemStack tabIconStack = this.itemStack;

        return (CreativeTab) (Object) new CreativeTabs(CreativeTabs.getNextID(), id) {
            @Override
            public net.minecraft.item.ItemStack getTabIconItem() {
                return (net.minecraft.item.ItemStack) (Object) tabIconStack;
            }
        };
    }

    @Override
    public CreativeTab.Builder tabIcon(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    @Override
    public CreativeTab.Builder from(CreativeTab value) {
        this.itemStack = value.getTabIcon();
        return this;
    }

    @Override
    public CreativeTab.Builder reset() {
        this.itemStack = (ItemStack) (Object) net.minecraft.item.ItemStack.EMPTY;
        return this;
    }
}
