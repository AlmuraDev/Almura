/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.deadbush.processor;

import com.almuradev.content.type.generation.type.feature.deadbush.DeadBushGenerator;
import com.almuradev.content.type.generation.type.feature.deadbush.DeadBushGeneratorConfig;
import com.almuradev.content.util.ConfigurateSucks;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.stream.Collectors;

public final class WorldsProcessor implements AbstractDeadBushProcessor {

    private static final ConfigTag TAG = ConfigTag.create(DeadBushGeneratorConfig.WORLDS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final DeadBushGenerator.Builder builder) {
        builder.worlds(ConfigurateSucks.children(config).stream().map(ConfigurationNode::getString).collect(Collectors.toList()));
    }
}
