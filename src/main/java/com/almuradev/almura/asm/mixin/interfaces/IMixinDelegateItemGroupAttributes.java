/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.shared.registry.catalog.CatalogDelegate;
import org.spongepowered.api.item.ItemType;

public interface IMixinDelegateItemGroupAttributes {

    void setItemTypeDelegate(CatalogDelegate<ItemType> itemTypeDelegate);
}
