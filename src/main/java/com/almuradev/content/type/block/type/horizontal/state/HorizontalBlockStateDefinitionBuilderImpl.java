/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;
import net.minecraft.util.EnumFacing;

public final class HorizontalBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<HorizontalBlockStateDefinition, HorizontalBlockStateDefinitionBuilderImpl> implements HorizontalBlockStateDefinitionBuilder {
    public final EnumFacing facing;

    public HorizontalBlockStateDefinitionBuilderImpl(final EnumFacing facing) {
        this.facing = facing;
    }

    @Override
    public HorizontalBlockStateDefinition build0() {
        return new HorizontalBlockStateDefinition(this);
    }
}
