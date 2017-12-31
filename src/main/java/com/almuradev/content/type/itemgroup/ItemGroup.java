/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.item.definition.ItemDefinition;
import net.minecraft.item.ItemStack;

public interface ItemGroup extends CatalogedContent {

    ItemStack icon();

    interface Builder extends ContentBuilder<ItemGroup> {

        void icon(final ItemDefinition icon);

        void sort(final boolean sort);
    }
}
