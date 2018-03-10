/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.block.facet.BlockExperience;
import com.almuradev.content.type.block.processor.AABBBlockContentProcessor;
import com.almuradev.content.type.block.processor.BlockFaceShapeProcessor;
import com.almuradev.content.type.block.processor.DestroyActionBlockContentProcessor;
import com.almuradev.content.type.block.processor.HardnessBlockContentProcessor;
import com.almuradev.content.type.block.processor.ItemGroupBlockContentProcessor;
import com.almuradev.content.type.block.processor.LightBlockContentProcessor;
import com.almuradev.content.type.block.processor.MapColorBlockContentProcessor;
import com.almuradev.content.type.block.processor.MaterialBlockContentProcessor;
import com.almuradev.content.type.block.processor.ParentBlockContentProcessor;
import com.almuradev.content.type.block.processor.ResistanceBlockContentProcessor;
import com.almuradev.content.type.block.processor.SoundBlockContentProcessor;
import com.almuradev.content.type.block.type.container.ContainerBlockModule;
import com.almuradev.content.type.block.type.crop.CropBlockModule;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockModule;
import com.almuradev.content.type.block.type.normal.NormalBlockModule;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class BlockModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("block", BlockContentTypeLoader.class));
        this.facet().add(BlockContentTypeLoader.class);
        this.install(new ContainerBlockModule());
        this.install(new CropBlockModule());
        this.install(new HorizontalBlockModule());
        this.install(new NormalBlockModule());
        this.facet()
                .add(BlockExperience.class);
        this.install(new Module() {
            @Override
            protected void configure() {
                this.processors()
                        .all(AABBBlockContentProcessor.class)
                        .all(DestroyActionBlockContentProcessor.class)
                        .all(BlockFaceShapeProcessor.class)
                        .all(HardnessBlockContentProcessor.class)
                        .all(ItemGroupBlockContentProcessor.class)
                        .all(LightBlockContentProcessor.class)
                        .all(MapColorBlockContentProcessor.class)
                        .all(MaterialBlockContentProcessor.class)
                        .all(ResistanceBlockContentProcessor.class)
                        .all(SoundBlockContentProcessor.class)
                        .all(ParentBlockContentProcessor.class);
            }
        });
    }

    public static abstract class Module extends AbstractModule implements CommonBinder {
        protected final MultiTypeProcessorBinder<BlockGenre, ContentBlockType, ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockContentProcessor<ContentBlockType, ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>> processors() {
            return new MultiTypeProcessorBinder<>(
                    this.binder(),
                    BlockGenre.values(),
                    new TypeLiteral<BlockGenre>() {},
                    new TypeLiteral<BlockContentProcessor<ContentBlockType, ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>>() {}
            );
        }
    }
}
