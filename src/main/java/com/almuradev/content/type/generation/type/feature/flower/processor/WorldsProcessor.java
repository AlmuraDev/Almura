/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.flower.processor;

import com.almuradev.content.type.generation.type.feature.flower.FlowerGenerator;
import com.almuradev.content.type.generation.type.feature.flower.FlowerGeneratorConfig;
import com.almuradev.content.util.ConfigurateSucks;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.stream.Collectors;

public final class WorldsProcessor implements AbstractFlowerProcessor {

    private static final ConfigTag TAG = ConfigTag.create(FlowerGeneratorConfig.WORLDS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final FlowerGenerator.Builder builder) {
        builder.worlds(ConfigurateSucks.children(config).stream().map(ConfigurationNode::getString).collect(Collectors.toList()));
    }
}
