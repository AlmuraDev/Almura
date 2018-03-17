/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class StairBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<StairBlockStateDefinition, StairBlockStateDefinitionBuilderImpl> implements StairBlockStateDefinitionBuilder {
    @Override
    public StairBlockStateDefinition build0() {
        return new StairBlockStateDefinition(this);
    }
}
