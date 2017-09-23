/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.block.facet.BlockExperience;
import com.almuradev.content.type.block.processor.AABBBlockContentProcessor;
import com.almuradev.content.type.block.processor.DestroyActionBlockContentProcessor;
import com.almuradev.content.type.block.processor.HardnessBlockContentProcessor;
import com.almuradev.content.type.block.processor.ItemGroupBlockContentProcessor;
import com.almuradev.content.type.block.processor.LightBlockContentProcessor;
import com.almuradev.content.type.block.processor.MapColorBlockContentProcessor;
import com.almuradev.content.type.block.processor.MaterialBlockContentProcessor;
import com.almuradev.content.type.block.processor.ParentBlockContentProcessor;
import com.almuradev.content.type.block.processor.ResistanceBlockContentProcessor;
import com.almuradev.content.type.block.processor.SoundBlockContentProcessor;
import com.almuradev.content.type.block.type.container.ContainerBlock;
import com.almuradev.content.type.block.type.container.ContainerBlockBuilder;
import com.almuradev.content.type.block.type.container.processor.LimitContainerBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.CropBlockBuilder;
import com.almuradev.content.type.block.type.crop.processor.CanRollbackCropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.FertilizerCropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.processor.growth.GrowthCropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.processor.hydration.HydrationCropBlockContentProcessor;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlock;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockBuilder;
import com.almuradev.content.type.block.type.normal.NormalBlock;
import com.almuradev.content.type.block.type.normal.NormalBlockBuilder;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class BlockModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.bind(ContainerBlock.Builder.class).to(ContainerBlockBuilder.class);
        this.bind(CropBlock.Builder.class).to(CropBlockBuilder.class);
        this.bind(HorizontalBlock.Builder.class).to(HorizontalBlockBuilder.class);
        this.bind(NormalBlock.Builder.class).to(NormalBlockBuilder.class);
        this.facet()
                .add(BlockContentTypeLoader.class)
                .add(BlockExperience.class);
        this.processors()
                .all(AABBBlockContentProcessor.class)
                .all(DestroyActionBlockContentProcessor.class)
                .all(HardnessBlockContentProcessor.class)
                .all(ItemGroupBlockContentProcessor.class)
                .all(LightBlockContentProcessor.class)
                .all(MapColorBlockContentProcessor.class)
                .all(MaterialBlockContentProcessor.class)
                .all(ResistanceBlockContentProcessor.class)
                .all(SoundBlockContentProcessor.class)
                .all(ParentBlockContentProcessor.class)
                .only(LimitContainerBlockContentProcessor.class, BlockGenre.CONTAINER)
                .only(CanRollbackCropBlockContentProcessor.class, BlockGenre.CROP)
                .only(GrowthCropBlockContentProcessor.class, BlockGenre.CROP)
                .only(HydrationCropBlockContentProcessor.class, BlockGenre.CROP)
                .only(FertilizerCropBlockContentProcessor.class, BlockGenre.CROP);
    }

    private MultiTypeProcessorBinder<BlockGenre, ContentBlockType, ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockContentProcessor<ContentBlockType, ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>> processors() {
        return new MultiTypeProcessorBinder<>(
                this.binder(),
                BlockGenre.values(),
                new TypeLiteral<BlockGenre>() {},
                new TypeLiteral<BlockContentProcessor<ContentBlockType, ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>>() {}
        );
    }
}
