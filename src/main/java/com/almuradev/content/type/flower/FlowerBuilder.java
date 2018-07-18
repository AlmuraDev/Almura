/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.flower;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.util.WeightedLazyBlockState;

import java.util.ArrayList;
import java.util.List;

public final class FlowerBuilder extends ContentBuilder.Impl<Flower> implements Flower.Builder {
    private List<WeightedLazyBlockState> flowers = new ArrayList<>();

    public void flower(List<WeightedLazyBlockState> flowers) {
        this.flowers.addAll(flowers);
    }

    @Override
    public Flower build() {
        final Flower flower = new FlowerFeature(this.flowers);
        ((IMixinSetCatalogTypeId) flower).setId(this.id, this.name);
        return flower;
    }
}
