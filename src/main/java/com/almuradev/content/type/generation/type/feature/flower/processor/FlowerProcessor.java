/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.flower.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.flower.Flower;
import com.almuradev.content.type.generation.type.feature.flower.FlowerGeneratorConfig;
import com.almuradev.content.type.generation.type.feature.flower.FlowerGenerator;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class FlowerProcessor implements AbstractFlowerProcessor {

    private static final ConfigTag TAG = ConfigTag.create(FlowerGeneratorConfig.FLOWER);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final FlowerGenerator.Builder builder) {
        builder.flower(CatalogDelegate.namespaced(Flower.class, config));
    }
}
