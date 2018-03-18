/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.ContentType;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.block.facet.BlockExperience;
import com.almuradev.content.type.block.processor.AABBProcessor;
import com.almuradev.content.type.block.processor.BlockFaceShapeProcessor;
import com.almuradev.content.type.block.processor.DestroyActionProcessor;
import com.almuradev.content.type.block.processor.HardnessProcessor;
import com.almuradev.content.type.block.processor.ItemGroupProcessor;
import com.almuradev.content.type.block.processor.LightProcessor;
import com.almuradev.content.type.block.processor.MapColorProcessor;
import com.almuradev.content.type.block.processor.MaterialProcessor;
import com.almuradev.content.type.block.processor.ParentProcessor;
import com.almuradev.content.type.block.processor.ResistanceProcessor;
import com.almuradev.content.type.block.processor.SoundProcessor;
import com.almuradev.content.type.block.type.container.ContainerBlockModule;
import com.almuradev.content.type.block.type.crop.CropBlockModule;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockModule;
import com.almuradev.content.type.block.type.leaf.LeafBlockModule;
import com.almuradev.content.type.block.type.log.LogBlockModule;
import com.almuradev.content.type.block.type.normal.NormalBlockModule;
import com.almuradev.content.type.block.type.sapling.SaplingBlockModule;
import com.almuradev.core.CoreBinder;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class BlockModule extends AbstractModule implements CoreBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("block", BlockContentTypeLoader.class));
        this.facet().add(BlockContentTypeLoader.class);
        this.install(new ContainerBlockModule());
        this.install(new CropBlockModule());
        this.install(new HorizontalBlockModule());
        this.install(new LeafBlockModule());
        this.install(new LogBlockModule());
        this.install(new NormalBlockModule());
        this.install(new SaplingBlockModule());
        this.facet()
                .add(BlockExperience.class);
        this.install(new Module() {
            @Override
            protected void configure() {
                this.processors()
                        .all(AABBProcessor.class)
                        .all(DestroyActionProcessor.class)
                        .all(BlockFaceShapeProcessor.class)
                        .all(HardnessProcessor.class)
                        .all(ItemGroupProcessor.class)
                        .all(LightProcessor.class)
                        .all(MapColorProcessor.class)
                        .all(MaterialProcessor.class)
                        .all(ResistanceProcessor.class)
                        .all(SoundProcessor.class)
                        .all(ParentProcessor.class);
            }
        });
    }

    public static abstract class Module extends AbstractModule implements CoreBinder {
        protected final MultiTypeProcessorBinder<BlockGenre, ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockContentProcessor<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>> processors() {
            return new MultiTypeProcessorBinder<>(
                    this.binder(),
                    BlockGenre.values(),
                    new TypeLiteral<BlockGenre>() {},
                    new TypeLiteral<BlockContentProcessor<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>>() {}
            );
        }
    }
}
