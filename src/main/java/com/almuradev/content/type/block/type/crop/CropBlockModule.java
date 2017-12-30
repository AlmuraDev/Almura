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
import com.almuradev.content.type.block.type.crop.processor.CanRollbackCropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.FertilizerCropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.processor.growth.GrowthCropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.processor.hydration.HydrationCropBlockContentProcessor;

public final class CropBlockModule extends BlockModule.Module {

    @Override
    protected void configure() {
        this.bind(CropBlock.Builder.class).to(CropBlockBuilder.class);
        this.processors()
                .only(CanRollbackCropBlockContentProcessor.class, BlockGenre.CROP)
                .only(GrowthCropBlockContentProcessor.class, BlockGenre.CROP)
                .only(HydrationCropBlockContentProcessor.class, BlockGenre.CROP)
                .only(FertilizerCropBlockContentProcessor.class, BlockGenre.CROP);
    }
}
