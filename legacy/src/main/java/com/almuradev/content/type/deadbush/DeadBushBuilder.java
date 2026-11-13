/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.deadbush;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.util.WeightedLazyBlockState;

import java.util.ArrayList;
import java.util.List;

public final class DeadBushBuilder extends ContentBuilder.Impl<DeadBush> implements DeadBush.Builder {
    private List<WeightedLazyBlockState> deadBushes = new ArrayList<>();

    public void deadBush(List<WeightedLazyBlockState> deadBushes) {
        this.deadBushes.addAll(deadBushes);
    }

    @Override
    public DeadBush build() {
        final DeadBush deadBush = new DeadBushFeature(this.deadBushes);
        ((IMixinSetCatalogTypeId) deadBush).setId(this.id, this.name);
        return deadBush;
    }
}
