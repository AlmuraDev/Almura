/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.item.definition.ItemDefinition;

final class ItemGroupBuilder extends ContentBuilder.Impl<ItemGroup> implements ItemGroup.Builder {
    private ItemDefinition icon;
    private boolean sort = true;

    @Override
    public void icon(final ItemDefinition icon) {
        this.icon = icon;
    }

    @Override
    public void sort(final boolean sort) {
        this.sort = sort;
    }

    @Override
    public ItemGroup build() {
        return (ItemGroup) (Object) new ItemGroupImpl(this.id, this.id.replace(':', '.'), this.icon, this.sort);
    }
}
