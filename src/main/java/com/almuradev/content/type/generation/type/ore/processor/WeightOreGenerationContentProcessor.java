/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore.processor;

import com.almuradev.content.type.generation.type.ore.OreGenerationConfig;
import com.almuradev.content.type.generation.type.ore.OreGenerator;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class WeightOreGenerationContentProcessor implements OreGenerationContentProcessor {
    private static final ConfigTag TAG = ConfigTag.create(OreGenerationConfig.WEIGHT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final OreGenerator.Builder builder) {
        builder.weight(config.getInt());
    }
}
