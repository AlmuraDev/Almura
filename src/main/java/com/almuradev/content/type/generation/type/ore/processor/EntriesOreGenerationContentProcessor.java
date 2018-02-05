/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.generation.type.ore.OreDefinition;
import com.almuradev.content.type.generation.type.ore.OreGenerationConfig;
import com.almuradev.content.type.generation.type.ore.OreGenerator;
import com.almuradev.toolbox.config.processor.AbstractArrayConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

import javax.inject.Inject;
import javax.inject.Provider;

public final class EntriesOreGenerationContentProcessor implements AbstractArrayConfigProcessor.EqualTreatment<OreGenerator.Builder>, OreGenerationContentProcessor {
    private static final ConfigTag TAG = ConfigTag.create(OreGenerationConfig.ENTRIES);
    private static final ConfigTag BLOCK = ConfigTag.create(OreGenerationConfig.Definition.BLOCK);
    private static final ConfigTag COUNT = ConfigTag.create(OreGenerationConfig.Definition.COUNT);
    private static final ConfigTag SIZE = ConfigTag.create(OreGenerationConfig.Definition.SIZE);
    private final Provider<OreDefinition.Builder> builder;

    @Inject
    private EntriesOreGenerationContentProcessor(final Provider<OreDefinition.Builder> builder) {
        this.builder = builder;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final OreGenerator.Builder context) {
        this.processChildren(config, context);
    }

    @Override
    public void processChild(final ConfigurationNode config, final int index, final OreGenerator.Builder builder) {
        final OreDefinition.Builder definition = this.builder.get();
        definition.block(CatalogDelegate.namespaced(BlockType.class, BLOCK.in(config)));
        definition.count(COUNT.in(config).getInt());
        definition.size(SIZE.in(config).getInt());
        builder.push(definition.build(builder));
    }
}
