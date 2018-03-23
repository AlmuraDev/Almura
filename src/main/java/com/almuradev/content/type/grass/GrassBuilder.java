/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.WeightedLazyBlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GrassBuilder extends ContentBuilder.Impl<Grass> implements Grass.Builder {
    private List<WeightedLazyBlockState> grasses = new ArrayList<>();

    @Override
    public void grass(List<WeightedLazyBlockState> grasses) {
        this.grasses.addAll(grasses);
    }

    @Override
    public Grass build() {
        Collections.shuffle(this.grasses);
        final Grass grass = new GrassFeature(this.grasses);
        ((IMixinSetCatalogTypeId) grass).setId(this.id, this.name);
        return grass;
    }
}
