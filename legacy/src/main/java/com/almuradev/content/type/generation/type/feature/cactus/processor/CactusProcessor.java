/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.cactus.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.generation.type.feature.cactus.CactusGenerator;
import com.almuradev.content.type.generation.type.feature.cactus.CactusGeneratorConfig;
import com.almuradev.content.type.cactus.Cactus;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class CactusProcessor implements AbstractCactusProcessor {

    private static final ConfigTag TAG = ConfigTag.create(CactusGeneratorConfig.CACTUS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final CactusGenerator.Builder builder) {
        builder.cactus(CatalogDelegate.namespaced(Cactus.class, config));
    }
}
