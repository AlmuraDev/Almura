/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.processor;

import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.type.generation.GenerationConfig;
import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class WeightProcessor implements GenerationContentProcessor<ContentGenerator, ContentGenerator.Builder<ContentGenerator>>, TaggedConfigProcessor<ContentGenerator.Builder<ContentGenerator>, ConfigTag> {
    private static final ConfigTag TAG = ConfigTag.create(GenerationConfig.WEIGHT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ContentGenerator.Builder<ContentGenerator> builder) {
        builder.weight(config.getInt());
    }
}
