/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.creativetab;

import com.almuradev.almura.api.BuildableCatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(CreativeTabs.class)
public interface CreativeTab extends BuildableCatalogType {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    ItemStack getTabIcon();

    interface Builder extends BuildableCatalogType.Builder<CreativeTab, Builder> {

        Builder tabIcon(ItemStack itemStack);
    }
}
