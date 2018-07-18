/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.slab;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.item.ContentItem;

public interface SlabItem extends ContentItem {
    interface Builder extends ContentItem.Builder<SlabItem> {
        void doubleBlock(final LazyBlockState doubleBlock);

        void singleBlock(final LazyBlockState singleBlock);
    }
}
