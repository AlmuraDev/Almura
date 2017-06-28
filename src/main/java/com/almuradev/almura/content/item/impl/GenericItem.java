/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.impl;

import com.almuradev.almura.content.item.BuildableItemType;
import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;

public class GenericItem extends Item {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((BuildableItemType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("itemGroup", ((BuildableItemType) (Object) this).getItemGroup().orElse(null))
                .toString();
    }
}
