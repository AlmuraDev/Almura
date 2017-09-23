/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal.state;

import com.almuradev.content.type.block.BlockStateDefinition;
import net.minecraft.util.EnumFacing;

public final class HorizontalBlockStateDefinitionBuilderImpl extends BlockStateDefinition.Builder.Impl<HorizontalBlockStateDefinition> implements HorizontalBlockStateDefinitionBuilder {

    public final EnumFacing facing;

    public HorizontalBlockStateDefinitionBuilderImpl(final EnumFacing facing) {
        this.facing = facing;
    }

    @Override
    public HorizontalBlockStateDefinition build() {
        return new HorizontalBlockStateDefinition(this);
    }
}
