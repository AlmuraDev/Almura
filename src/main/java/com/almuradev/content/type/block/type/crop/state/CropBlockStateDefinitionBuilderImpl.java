/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.state;

import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.Fertilizer;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.processor.hydration.Hydration;

import javax.annotation.Nullable;

public final class CropBlockStateDefinitionBuilderImpl extends BlockStateDefinition.Builder.Impl<CropBlockStateDefinition> implements CropBlockStateDefinitionBuilder {

    final int age;
    boolean canRollback;
    @Nullable Fertilizer fertilizer;
    @Nullable Growth growth;
    @Nullable Hydration hydration;

    public CropBlockStateDefinitionBuilderImpl(final int age) {
        this.age = age;
    }

    @Override
    public void canRollback(final boolean canRollback) {
        this.canRollback = canRollback;
    }

    @Override
    public void fertilizer(@Nullable final Fertilizer fertilizer) {
        this.fertilizer = fertilizer;
    }

    @Override
    public void growth(@Nullable final Growth growth) {
        this.growth = growth;
    }

    @Override
    public void hydration(@Nullable final Hydration hydration) {
        this.hydration = hydration;
    }

    @Override
    public CropBlockStateDefinition build() {
        return new CropBlockStateDefinition(this);
    }

    @Override
    public void inherit(BlockStateDefinition.Builder<CropBlockStateDefinition> builder) {
        super.inherit(builder);

        final CropBlockStateDefinitionBuilderImpl cropBuilder = (CropBlockStateDefinitionBuilderImpl) builder;

        // TODO CanRollback needs to be a tri-state to see if it has been actually "set"

        if (this.fertilizer == null) {
            this.fertilizer = cropBuilder.fertilizer;
        }

        if (this.growth == null) {
            this.growth = cropBuilder.growth;
        }

        if (this.hydration == null) {
            this.hydration = cropBuilder.hydration;
        }
    }
}
