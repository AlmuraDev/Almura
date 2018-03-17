/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.ItemGrouped;
import org.spongepowered.api.util.PEBKACException;

import java.util.Optional;

// This cannot extend ItemType.
public interface ContentItem extends CatalogedContent, ItemGrouped {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    @Override
    default Optional<ItemGroup> itemGroup() {
        throw new PEBKACException("mixin");
    }

    interface Builder<C extends ContentItem> extends ContentBuilder<C> {
        void durability(final int durability);

        void itemGroup(final Delegate<ItemGroup> itemGroup);

        void maxStackSize(final int maxStackSize);
    }

    interface Tier extends CatalogedContent {
        // TODO(kashike): support for custom tiers in the future
    }
}
