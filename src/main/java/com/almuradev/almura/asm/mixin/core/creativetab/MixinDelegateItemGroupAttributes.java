/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.creativetab;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateItemGroupAttributes;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.group.type.GenericItemGroup;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import net.minecraft.item.Item;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(GenericItemGroup.class)
public abstract class MixinDelegateItemGroupAttributes extends MixinCreativeTabs implements ItemGroup, IMixinDelegateItemGroupAttributes {

    @Nullable
    private CatalogDelegate<ItemType> itemTypeDelegate;

    @Override
    public ItemStack getIcon() {
        if (!this.iconItemStack.isEmpty() || this.itemTypeDelegate == null) {
            return (ItemStack) (Object) this.iconItemStack;
        }

        final ItemType itemType = this.itemTypeDelegate.get();
        this.iconItemStack = new net.minecraft.item.ItemStack((Item) itemType);

        return (ItemStack) (Object) this.iconItemStack;
    }

    @Override
    public void setItemTypeDelegate(CatalogDelegate<ItemType> itemTypeDelegate) {
        this.itemTypeDelegate = itemTypeDelegate;
    }
}
