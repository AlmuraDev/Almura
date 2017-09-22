/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.horizontal;

import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import net.minecraft.block.Block;

public final class HorizontalBlockTypeBuilder extends AbstractBlockTypeBuilder<HorizontalBlockType, HorizontalBlockTypeBuilder> implements HorizontalBlockType.Builder<HorizontalBlockType, HorizontalBlockTypeBuilder> {

    @Override
    protected Block createBlock(final HorizontalBlockTypeBuilder builder) {
        return new HorizontalBlock(builder);
    }
}
