/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.normal.state;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.content.type.block.state.AbstractBlockStateDefinitionBuilder;

public final class NormalBlockStateDefinitionBuilder extends AbstractBlockStateDefinitionBuilder<NormalBlockStateDefinitionBuilder> {

    @Override
    public NormalBlockStateDefinition build() {
        checkState(this.material != null, "material not set");
        checkState(this.mapColor != null, "map color not set");
        return new NormalBlockStateDefinition(this);
    }
}
