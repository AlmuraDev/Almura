/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class SlabBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<SlabBlockStateDefinition, SlabBlockStateDefinitionBuilderImpl> implements SlabBlockStateDefinitionBuilder {
    @Override
    public SlabBlockStateDefinition build0() {
        return new SlabBlockStateDefinition(this);
    }
}
