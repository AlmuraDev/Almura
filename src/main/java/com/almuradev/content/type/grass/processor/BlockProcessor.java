/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.grass.Grass;
import com.almuradev.content.type.grass.GrassConfig;
import com.almuradev.content.util.ConfigurateSucks;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.stream.Collectors;

public final class BlockProcessor implements AbstractGrassProcessor {
    private static final ConfigTag TAG = ConfigTag.create(GrassConfig.BLOCK);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(ConfigurationNode config, Grass.Builder builder) {
        builder.grass(ConfigurateSucks.children(config).stream().map(LazyBlockState::parse).collect(Collectors.toList()));
    }
}
