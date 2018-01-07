/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;

import java.util.ArrayList;
import java.util.List;

public final class OreGeneratorBuilder extends ContentBuilder.Impl<OreGenerator> implements OreGenerator.Builder {
    private final List<OreDefinition> definitions = new ArrayList<>();
    private int weight;

    @Override
    public void weight(final int weight) {
        this.weight = weight;
    }

    @Override
    public void push(final OreDefinition definition) {
        this.definitions.add(definition);
    }

    @Override
    public OreGenerator build() {
        final OreGenerator feature = new OreGeneratorImpl(this.weight, this.definitions);
        ((IMixinSetCatalogTypeId) feature).setId(this.id, this.name);
        return feature;
    }
}
