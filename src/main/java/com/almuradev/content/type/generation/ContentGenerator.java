/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import org.spongepowered.api.util.PEBKACException;

public interface ContentGenerator extends CatalogedContent {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    interface Weighted extends ContentGenerator {
        int weight();
    }

    interface Builder<C extends ContentGenerator> extends ContentBuilder<C> {
        void weight(final int weight);

        abstract class Impl<C extends ContentGenerator> extends ContentBuilder.Impl<C> implements Builder<C> {
            public int weight;

            @Override
            public void weight(final int weight) {
                this.weight = weight;
            }
        }
    }
}
