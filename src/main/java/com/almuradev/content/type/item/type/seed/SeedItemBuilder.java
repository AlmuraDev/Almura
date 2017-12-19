/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.item.ContentItemType;
import org.spongepowered.api.block.BlockType;

public final class SeedItemBuilder extends ContentItemType.Builder.Impl<SeedItem> implements SeedItem.Builder {

    Delegate<BlockType> crop;
    Delegate<BlockType> soil;

    @Override
    public void crop(final Delegate<BlockType> crop) {
        this.crop = crop;
    }

    @Override
    public void soil(final Delegate<BlockType> soil) {
        this.soil = soil;
    }

    @Override
    public SeedItem build() {
        checkNotNull(this.crop);
        checkNotNull(this.soil);
        return new SeedItemImpl(this);
    }
}
