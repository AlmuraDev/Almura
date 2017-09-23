/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.item.type.generic;

import com.almuradev.almura.content.type.item.type.BuildableItemType;
import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import org.spongepowered.api.item.ItemType;

public class GenericItem extends Item {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((ItemType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("itemGroup", ((BuildableItemType) (Object) this).getItemGroup().orElse(null))
                .toString();
    }
}
