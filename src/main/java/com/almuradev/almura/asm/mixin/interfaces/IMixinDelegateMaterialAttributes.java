/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

public interface IMixinDelegateMaterialAttributes {

    void setItemGroupDelegate(CatalogDelegate<ItemGroup> itemGroupDelegate);
}
