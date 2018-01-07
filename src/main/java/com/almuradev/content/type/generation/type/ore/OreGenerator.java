/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import com.almuradev.content.type.generation.ContentGenerator;
import org.spongepowered.api.util.PEBKACException;

public interface OreGenerator extends ContentGenerator {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    interface Builder extends ContentGenerator.Builder<OreGenerator> {
        void weight(final int weight);

        void push(final OreDefinition definition);
    }
}
