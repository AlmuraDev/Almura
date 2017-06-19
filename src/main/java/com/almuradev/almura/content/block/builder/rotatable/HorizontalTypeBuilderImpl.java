/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.builder.rotatable;

import com.almuradev.almura.content.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.content.block.impl.rotatable.GenericHorizontalBlock;
import com.almuradev.almura.content.block.rotatable.HorizontalType;
import net.minecraft.block.Block;

public class HorizontalTypeBuilderImpl extends AbstractBlockTypeBuilder<HorizontalType, HorizontalTypeBuilderImpl> implements
        HorizontalType.Builder<HorizontalType, HorizontalTypeBuilderImpl> {

    @Override
    protected Block createBlock(HorizontalTypeBuilderImpl builder) {
        return new GenericHorizontalBlock(builder);
    }
}
