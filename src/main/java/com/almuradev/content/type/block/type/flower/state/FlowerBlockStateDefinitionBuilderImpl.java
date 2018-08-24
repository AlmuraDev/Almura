/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.flower.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class FlowerBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<FlowerBlockStateDefinition, FlowerBlockStateDefinitionBuilderImpl> implements FlowerBlockStateDefinitionBuilder {
    @Override
    public FlowerBlockStateDefinition build0() {
        return new FlowerBlockStateDefinition(this);
    }
}
