/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.Fertilizer;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.processor.hydration.Hydration;

import javax.annotation.Nullable;

public final class CropBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<CropBlockStateDefinition, CropBlockStateDefinitionBuilderImpl> implements CropBlockStateDefinitionBuilder {
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
    public CropBlockStateDefinition build0() {
        return new CropBlockStateDefinition(this);
    }

    @Override
    protected void inherit(final CropBlockStateDefinitionBuilderImpl that) {
        super.inherit(that);
        that.canRollback = this.canRollback;
        // Only what the child doesn't set itself, so a stage can override its parent (e.g. a slower growth chance)
        if (that.fertilizer == null && this.fertilizer != null) that.fertilizer = this.fertilizer;
        if (that.growth == null && this.growth != null) that.growth = this.growth;
        if (that.hydration == null && this.hydration != null) that.hydration = this.hydration;
    }
}
