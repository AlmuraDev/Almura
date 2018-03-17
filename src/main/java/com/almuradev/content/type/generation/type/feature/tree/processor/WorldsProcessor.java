/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree.processor;

import com.almuradev.content.type.generation.type.feature.tree.TreeGenerator;
import com.almuradev.content.type.generation.type.feature.tree.TreeGeneratorConfig;
import com.almuradev.content.util.ConfigureSucks;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.stream.Collectors;

public final class WorldsProcessor implements AbstractTreeProcessor {
    private static final ConfigTag TAG = ConfigTag.create(TreeGeneratorConfig.WORLDS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final TreeGenerator.Builder builder) {
        builder.worlds(ConfigureSucks.children(config).stream().map(ConfigurationNode::getString).collect(Collectors.toList()));
    }
}
