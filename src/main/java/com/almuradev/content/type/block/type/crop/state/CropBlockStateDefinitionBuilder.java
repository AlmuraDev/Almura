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

public interface CropBlockStateDefinitionBuilder extends BlockStateDefinition.Builder<CropBlockStateDefinition> {
    void canRollback(boolean canRollback);

    void fertilizer(@Nullable final Fertilizer fertilizer);

    void growth(@Nullable final Growth growth);

    void hydration(@Nullable final Hydration hydration);
}
