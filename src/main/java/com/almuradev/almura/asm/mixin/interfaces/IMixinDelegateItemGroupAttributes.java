/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.registry.CatalogDelegate;
import org.spongepowered.api.item.ItemType;

public interface IMixinDelegateItemGroupAttributes {

    void setItemTypeDelegate(CatalogDelegate<ItemType> itemTypeDelegate);
}
