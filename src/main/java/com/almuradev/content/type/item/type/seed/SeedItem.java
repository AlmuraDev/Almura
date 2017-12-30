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
import com.almuradev.content.type.item.type.seed.processor.grass.Grass;
import org.spongepowered.api.block.BlockType;

import javax.annotation.Nullable;

public interface SeedItem extends ContentItemType {

    @Nullable Grass getGrass();

    interface Builder extends ContentItemType.Builder<SeedItem> {

        void crop(final Delegate<BlockType> crop);

        void soil(final Delegate<BlockType> soil);

        void grass(final Grass grass);
    }
}
