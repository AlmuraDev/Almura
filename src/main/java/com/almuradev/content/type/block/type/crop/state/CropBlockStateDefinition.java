/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.state;

import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.Fertilizer;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.processor.hydration.Hydration;

import javax.annotation.Nullable;

public final class CropBlockStateDefinition extends BlockStateDefinition.Impl<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {

    public final int age;
    public final boolean canRollback;
    @Nullable public final Fertilizer fertilizer;
    @Nullable public final Growth growth;
    @Nullable public final Hydration hydration;

    CropBlockStateDefinition(final CropBlockStateDefinitionBuilderImpl builder) {
        super(builder);
        this.age = builder.age;
        this.canRollback = builder.canRollback;
        this.growth = builder.growth;
        this.hydration = builder.hydration;
        this.fertilizer = builder.fertilizer;
    }

}
