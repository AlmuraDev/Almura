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
import com.almuradev.content.util.WeightedLazyBlockState;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.stream.Collectors;

public final class BlockProcessor implements AbstractGrassProcessor {

    private static final ConfigTag TAG = ConfigTag.create(GrassConfig.GRASSES);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(ConfigurationNode config, Grass.Builder builder) {
        ConfigurateSucks.children(config).forEach(node -> {
            final int weight = node.getNode(GrassConfig.WEIGHT).getInt(1);
            final List<WeightedLazyBlockState> states = ConfigurateSucks.children(node.getNode(GrassConfig.BLOCK))
                    .stream()
                    .map(LazyBlockState::parse)
                    .map(lazyState -> new WeightedLazyBlockState(weight, lazyState))
                    .collect(Collectors.toList());
            builder.grass(states);
        });
    }
}
