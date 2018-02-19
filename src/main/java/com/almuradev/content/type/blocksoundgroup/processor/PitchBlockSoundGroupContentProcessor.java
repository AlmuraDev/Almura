/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup.processor;

import static com.almuradev.toolbox.config.ConfigurationNodes.whenRealDouble;

import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupConfig;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class PitchBlockSoundGroupContentProcessor implements TaggedConfigProcessor<BlockSoundGroup.Builder, ConfigTag> {
    private static final ConfigTag TAG = ConfigTag.create(BlockSoundGroupConfig.PITCH);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final BlockSoundGroup.Builder builder) {
        whenRealDouble(config, builder::pitch);
    }
}
