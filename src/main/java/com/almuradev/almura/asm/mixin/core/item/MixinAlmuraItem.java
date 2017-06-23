/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.item;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.item.impl.GenericItem;
import com.almuradev.almura.content.loader.CatalogDelegate;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = {GenericItem.class})
public abstract class MixinAlmuraItem extends MixinItem implements BuildableItemType, IMixinDelegateMaterialAttributes {

    @Nullable private CatalogDelegate<ItemGroup> itemGroupDelegate;

    @Override
    public Optional<ItemGroup> getItemGroup() {
        if (this.tabToDisplayOn != null) {
            return Optional.of((ItemGroup) this.tabToDisplayOn);
        }

        // Having no delegate instance means it was truly null
        if (this.itemGroupDelegate == null) {
            return Optional.empty();
        }

        final ItemGroup cached = this.itemGroupDelegate.getCatalog();
        this.tabToDisplayOn = (CreativeTabs) (Object) cached;

        return Optional.of((ItemGroup) this.tabToDisplayOn);
    }

    @Override
    public void setItemGroupDelegate(CatalogDelegate<ItemGroup> itemGroupDelegate) {
        this.itemGroupDelegate = itemGroupDelegate;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab() {
        return (CreativeTabs) (Object) this.getItemGroup().orElse(null);
    }
}
