/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.deadbush.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.deadbush.DeadBushConfig;
import com.almuradev.content.type.deadbush.DeadBush;
import com.almuradev.content.util.ConfigurateSucks;
import com.almuradev.content.util.WeightedLazyBlockState;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.stream.Collectors;

public final class BlockProcessor implements AbstractDeadBushProcessor {

    private static final ConfigTag TAG = ConfigTag.create(DeadBushConfig.DEADBUSHES);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(ConfigurationNode config, DeadBush.Builder builder) {
        ConfigurateSucks.children(config).forEach(node -> {
            final int weight = node.getNode(DeadBushConfig.WEIGHT).getInt(1);
            final List<WeightedLazyBlockState> states = ConfigurateSucks.children(node.getNode(DeadBushConfig.BLOCK))
                    .stream()
                    .map(LazyBlockState::parse)
                    .map(lazyState -> new WeightedLazyBlockState(weight, lazyState))
                    .collect(Collectors.toList());
            builder.deadBush(states);
        });
    }
}
