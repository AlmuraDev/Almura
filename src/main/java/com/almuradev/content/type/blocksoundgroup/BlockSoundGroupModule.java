/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.loader.ContentFinder;
import com.almuradev.content.loader.SingleTypeExternalContentProcessor;
import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.blocksoundgroup.processor.ParentBlockSoundGroupContentPostProcessor;
import com.almuradev.content.type.blocksoundgroup.processor.PitchBlockSoundGroupContentProcessor;
import com.almuradev.content.type.blocksoundgroup.processor.SoundBlockSoundGroupContentProcessor;
import com.almuradev.content.type.blocksoundgroup.processor.VolumeBlockSoundGroupContentProcessor;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class BlockSoundGroupModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(BlockSoundGroupContentTypeLoader.class);
        this.bind(new TypeLiteral<ContentFinder<BlockSoundGroup, BlockSoundGroup.Builder>>() {}).to(BlockSoundGroupContentTypeLoader.class);
        this.bind(new TypeLiteral<SingleTypeExternalContentProcessor<BlockSoundGroup, BlockSoundGroup.Builder>>() {}).to(BlockSoundGroupContentTypeLoader.class);
        this.bind(BlockSoundGroup.Builder.class).to(BlockSoundGroupBuilder.class);
        this.registry().module(BlockSoundGroup.class, BlockSoundGroupRegistryModule.class);
        this.processors()
                .add(ParentBlockSoundGroupContentPostProcessor.class)
                .add(PitchBlockSoundGroupContentProcessor.class)
                .add(SoundBlockSoundGroupContentProcessor.class)
                .add(VolumeBlockSoundGroupContentProcessor.class);
    }

    private SingleTypeProcessorBinder<BlockSoundGroup, BlockSoundGroup.Builder, ConfigProcessor<? extends BlockSoundGroup.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends BlockSoundGroup.Builder>>() {});
    }
}
