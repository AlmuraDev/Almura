/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.cactus;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.util.WeightedLazyBlockState;
import org.spongepowered.api.util.PEBKACException;

import java.util.List;

public interface Cactus extends CatalogedContent {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    interface Builder extends ContentBuilder<Cactus> {
        void cactus(final List<WeightedLazyBlockState> cacti);
    }
}
