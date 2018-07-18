/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup.processor;

import com.almuradev.content.loader.ContentFinder;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupConfig;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import javax.inject.Inject;

public final class ParentBlockSoundGroupContentPostProcessor implements TaggedConfigProcessor<BlockSoundGroup.Builder, ConfigTag> {
    private static final ConfigTag TAG = ConfigTag.create(BlockSoundGroupConfig.PARENT);
    private final ContentFinder<BlockSoundGroup, BlockSoundGroup.Builder> finder;

    @Inject
    public ParentBlockSoundGroupContentPostProcessor(final ContentFinder<BlockSoundGroup, BlockSoundGroup.Builder> finder) {
        this.finder = finder;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final BlockSoundGroup.Builder builder) {
        // NOOP
    }

    @Override
    public void postProcessTagged(final ConfigurationNode config, final BlockSoundGroup.Builder builder) {
        this.finder.findBuilder(config.getString()).ifPresent(builder::inherit);
    }
}
