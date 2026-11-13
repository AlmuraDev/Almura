/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.cactus;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.util.WeightedLazyBlockState;

import java.util.ArrayList;
import java.util.List;

public final class CactusBuilder extends ContentBuilder.Impl<Cactus> implements Cactus.Builder {
    private List<WeightedLazyBlockState> cacti = new ArrayList<>();

    @Override
    public void cactus(List<WeightedLazyBlockState> cacti) {
        this.cacti.addAll(cacti);
    }

    @Override
    public Cactus build() {
        final Cactus cactus = new CactusFeature(this.cacti);
        ((IMixinSetCatalogTypeId) cactus).setId(this.id, this.name);
        return cactus;
    }
}
