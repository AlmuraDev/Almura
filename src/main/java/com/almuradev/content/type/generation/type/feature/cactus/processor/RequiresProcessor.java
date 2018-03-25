/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.cactus.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.generation.type.feature.cactus.CactusGenerator;
import com.almuradev.content.type.generation.type.feature.cactus.CactusGeneratorConfig;
import com.almuradev.content.util.ConfigurateSucks;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.stream.Collectors;

public final class RequiresProcessor implements AbstractCactusProcessor {

    private static final ConfigTag TAG = ConfigTag.create(CactusGeneratorConfig.REQUIRES);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final CactusGenerator.Builder builder) {
        builder.requires(ConfigurateSucks.children(config).stream().map(LazyBlockState::parse).collect(Collectors.toList()));
    }
}
