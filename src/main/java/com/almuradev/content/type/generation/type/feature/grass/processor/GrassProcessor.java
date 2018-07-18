/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.grass.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.generation.type.feature.grass.GrassGenerator;
import com.almuradev.content.type.generation.type.feature.grass.GrassGeneratorConfig;
import com.almuradev.content.type.grass.Grass;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class GrassProcessor implements AbstractGrassProcessor {

    private static final ConfigTag TAG = ConfigTag.create(GrassGeneratorConfig.GRASS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final GrassGenerator.Builder builder) {
        builder.grass(CatalogDelegate.namespaced(Grass.class, config));
    }
}
