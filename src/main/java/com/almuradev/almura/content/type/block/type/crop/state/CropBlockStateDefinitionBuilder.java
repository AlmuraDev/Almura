/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.crop.state;

import com.almuradev.almura.content.type.block.state.AbstractBlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.state.factory.AbstractBlockStateDefinitionBuilderFactory;

public final class CropBlockStateDefinitionBuilder extends AbstractBlockStateDefinitionBuilder<CropBlockStateDefinitionBuilder> {

    public CropBlockStateDefinitionBuilder() {
    }

    @Override
    public CropBlockStateDefinition build() {
        return new CropBlockStateDefinition(this);
    }

    public static abstract class Factory extends AbstractBlockStateDefinitionBuilderFactory<CropBlockStateDefinitionBuilder> {
    }
}
