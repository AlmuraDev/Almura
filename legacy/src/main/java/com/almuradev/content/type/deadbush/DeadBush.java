/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.deadbush;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.util.WeightedLazyBlockState;
import org.spongepowered.api.util.PEBKACException;

import java.util.List;

public interface DeadBush extends CatalogedContent {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    interface Builder extends ContentBuilder<DeadBush> {
        void deadBush(final List<WeightedLazyBlockState> deadBushes);
    }
}
