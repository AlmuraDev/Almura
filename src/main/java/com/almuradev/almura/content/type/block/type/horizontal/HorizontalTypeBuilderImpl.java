/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.type.horizontal;

import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import net.minecraft.block.Block;

public class HorizontalTypeBuilderImpl extends AbstractBlockTypeBuilder<HorizontalType, HorizontalTypeBuilderImpl> implements
        HorizontalType.Builder<HorizontalType, HorizontalTypeBuilderImpl> {

    @Override
    protected Block createBlock(HorizontalTypeBuilderImpl builder) {
        return new GenericHorizontalBlock(builder);
    }
}
