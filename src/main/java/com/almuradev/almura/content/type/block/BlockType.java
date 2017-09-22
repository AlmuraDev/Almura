/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block;

import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.type.crop.CropBlock;
import com.almuradev.almura.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.type.normal.NormalBlock;
import com.almuradev.almura.content.type.block.type.normal.state.NormalBlockStateDefinitionBuilder;

import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * An enumeration of supported block types.
 */
public enum BlockType {
    /**
     * @see NormalBlock
     */
    NORMAL(NormalBlockStateDefinitionBuilder::new),
    /**
     * @see CropBlock
     */
    CROP(CropBlockStateDefinitionBuilder::new);

    /**
     * The block state definition builder supplier.
     */
    private final Supplier<BlockStateDefinitionBuilder<?>> builder;

    BlockType(final Supplier<BlockStateDefinitionBuilder<?>> builder) {
        this.builder = builder;
    }

    public BlockStateDefinitionBuilder<?> builder() {
        return this.builder.get();
    }

    public static BlockType of(@Nullable final String string) {
        if(string == null) {
            throw new IllegalStateException("block is missing type");
        }
        return valueOf(string.toUpperCase(Locale.ENGLISH));
    }
}
