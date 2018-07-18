/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.block.type.crop.processor.CanRollbackProcessor;
import com.almuradev.content.type.block.type.crop.processor.SeedProcessor;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.FertilizerProcessor;
import com.almuradev.content.type.block.type.crop.processor.growth.GrowthProcessor;
import com.almuradev.content.type.block.type.crop.processor.hydration.HydrationProcessor;

public final class CropBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(CropBlock.Builder.class).to(CropBlockBuilder.class);
        this.processors()
                .only(CanRollbackProcessor.class, BlockGenre.CROP)
                .only(FertilizerProcessor.class, BlockGenre.CROP)
                .only(GrowthProcessor.class, BlockGenre.CROP)
                .only(HydrationProcessor.class, BlockGenre.CROP)
                .only(SeedProcessor.class, BlockGenre.CROP);
    }
}
