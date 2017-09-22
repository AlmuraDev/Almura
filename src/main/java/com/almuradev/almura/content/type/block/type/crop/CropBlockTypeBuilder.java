/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.crop;

import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import net.minecraft.block.Block;

public final class CropBlockTypeBuilder extends AbstractBlockTypeBuilder<CropBlockType, CropBlockTypeBuilder> implements CropBlockType.Builder<CropBlockType, CropBlockTypeBuilder> {

    @Override
    protected Block createBlock(final CropBlockTypeBuilder builder) {
        return new CropBlock(builder);
    }
}
