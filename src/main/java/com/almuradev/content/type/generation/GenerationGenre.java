/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.content.type.MultiContentType;
import com.almuradev.content.type.generation.type.feature.grass.GrassGenerator;
import com.almuradev.content.type.generation.type.feature.tree.TreeGenerator;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreGenerator;

@MultiContentType.Name("generators")
public enum GenerationGenre implements MultiContentType<ContentGenerator, ContentGenerator.Builder<ContentGenerator>> {
    /**
     * A generator type representing a tree.
     */
    TREE_FEATURE("feature/tree", "tree feature", TreeGenerator.Builder.class),
    /**
     * A generator type representing grass.
     */
    GRASS_FEATURE("feature/grass", "grass feature", GrassGenerator.Builder.class),
    /**
     * A generator type representing ore.
     */
    UNDERGROUND_ORE("underground/ore", "underground ore", UndergroundOreGenerator.Builder.class);

    /**
     * The id of this item type.
     *
     * <p>The id is used for identification and loading.</p>
     */
    private final String id;
    private final String friendlyName;
    private final Class<? extends ContentGenerator.Builder<? extends ContentGenerator>> builder;

    GenerationGenre(final String id, final String friendlyName, final Class<? extends ContentGenerator.Builder<? extends ContentGenerator>> builder) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.builder = builder;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String friendlyName() {
        return this.friendlyName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ContentGenerator.Builder<ContentGenerator>> builder() {
        return (Class<ContentGenerator.Builder<ContentGenerator>>) this.builder;
    }
}
