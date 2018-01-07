/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.content.ContentType;
import com.almuradev.content.type.generation.type.ore.OreGenerator;

@ContentType.MultiType.Name("generators")
public enum GenerationGenre implements ContentType.MultiType<ContentGenerator, ContentGenerator.Builder<ContentGenerator>> {
    /**
     * A generator type representing ore.
     */
    ORE("ore", OreGenerator.Builder.class);

    /**
     * The id of this item type.
     *
     * <p>The id is used for identification and loading.</p>
     */
    private final String id;
    private final Class<? extends ContentGenerator.Builder<? extends ContentGenerator>> builder;

    GenerationGenre(final String id, final Class<? extends ContentGenerator.Builder<? extends ContentGenerator>> builder) {
        this.id = id;
        this.builder = builder;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ContentGenerator.Builder<ContentGenerator>> builder() {
        return (Class<ContentGenerator.Builder<ContentGenerator>>) this.builder;
    }
}
