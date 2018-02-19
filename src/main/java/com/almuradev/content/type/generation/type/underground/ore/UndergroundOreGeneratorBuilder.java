/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;

import java.util.ArrayList;
import java.util.List;

public final class UndergroundOreGeneratorBuilder extends ContentBuilder.Impl<UndergroundOreGenerator> implements UndergroundOreGenerator.Builder {
    private final List<UndergroundOreDefinition> definitions = new ArrayList<>();
    private int weight;

    @Override
    public void weight(final int weight) {
        this.weight = weight;
    }

    @Override
    public void push(final UndergroundOreDefinition definition) {
        this.definitions.add(definition);
    }

    @Override
    public UndergroundOreGenerator build() {
        final UndergroundOreGenerator feature = new UndergroundOreGeneratorImpl(this.weight, this.definitions);
        ((IMixinSetCatalogTypeId) feature).setId(this.id, this.name);
        return feature;
    }
}
