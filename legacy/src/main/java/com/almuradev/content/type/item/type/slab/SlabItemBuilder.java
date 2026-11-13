/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.slab;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.item.AbstractItemBuilder;

public final class SlabItemBuilder extends AbstractItemBuilder<SlabItem> implements SlabItem.Builder {
    LazyBlockState doubleBlock;
    LazyBlockState singleBlock;

    @Override
    public void doubleBlock(final LazyBlockState doubleBlock) {
        this.doubleBlock = doubleBlock;
    }

    @Override
    public void singleBlock(final LazyBlockState singleBlock) {
        this.singleBlock = singleBlock;
    }

    @Override
    public SlabItem build() {
        return new SlabItemImpl(this);
    }
}
