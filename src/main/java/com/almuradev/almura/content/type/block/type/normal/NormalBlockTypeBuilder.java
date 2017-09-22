/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.normal;

import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import net.minecraft.block.Block;

public final class NormalBlockTypeBuilder extends AbstractBlockTypeBuilder<NormalBlockType, NormalBlockTypeBuilder> implements NormalBlockType.Builder<NormalBlockType, NormalBlockTypeBuilder> {

    @Override
    protected Block createBlock(NormalBlockTypeBuilder builder) {
        return new NormalBlock(builder);
    }
}
