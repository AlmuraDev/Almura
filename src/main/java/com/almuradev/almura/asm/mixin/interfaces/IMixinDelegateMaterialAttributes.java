/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.registry.CatalogDelegate;

public interface IMixinDelegateMaterialAttributes {

    void setItemGroupDelegate(CatalogDelegate<ItemGroup> itemGroupDelegate);
}
