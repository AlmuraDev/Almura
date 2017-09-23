/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.item.ContentItemType;
import org.spongepowered.api.block.BlockType;

public interface SeedItem extends ContentItemType {

    interface Builder extends ContentItemType.Builder<SeedItem> {

        void crop(final Delegate<BlockType> crop);

        void soil(final Delegate<BlockType> soil);
    }
}
