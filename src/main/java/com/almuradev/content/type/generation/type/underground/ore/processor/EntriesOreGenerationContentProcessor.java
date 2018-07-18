/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreDefinition;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreGenerationConfig;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreGenerator;
import com.almuradev.toolbox.config.processor.AbstractArrayConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import javax.inject.Inject;
import javax.inject.Provider;

public final class EntriesOreGenerationContentProcessor implements AbstractArrayConfigProcessor.EqualTreatment<UndergroundOreGenerator.Builder>, OreGenerationContentProcessor {
    private static final ConfigTag TAG = ConfigTag.create(UndergroundOreGenerationConfig.ENTRIES);
    private static final ConfigTag BLOCK = ConfigTag.create(UndergroundOreGenerationConfig.Definition.BLOCK);
    private static final ConfigTag COUNT = ConfigTag.create(UndergroundOreGenerationConfig.Definition.COUNT);
    private static final ConfigTag SIZE = ConfigTag.create(UndergroundOreGenerationConfig.Definition.SIZE);
    private static final ConfigTag WORLDS = ConfigTag.create(UndergroundOreGenerationConfig.Definition.WORLDS);
    private final Provider<UndergroundOreDefinition.Builder> builder;

    @Inject
    private EntriesOreGenerationContentProcessor(final Provider<UndergroundOreDefinition.Builder> builder) {
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
    public void processTagged(final ConfigurationNode config, final UndergroundOreGenerator.Builder context) {
        this.processChildren(config, context);
    }

    @Override
    public void processChild(final ConfigurationNode config, final int index, final UndergroundOreGenerator.Builder builder) {
        final UndergroundOreDefinition.Builder definition = this.builder.get();
        definition.block(LazyBlockState.parse(BLOCK.in(config)));
        definition.count(COUNT.in(config).getInt());
        definition.size(SIZE.in(config).getInt());
        definition.worlds(WORLDS.in(config).getList(Types::asString));
        builder.push(definition.build(builder));
    }
}
