/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.ContentBuilder;
import net.minecraft.world.WorldProvider;
import org.spongepowered.api.block.BlockType;

final class OreDefinitionImpl implements OreDefinition {
    private final Delegate<BlockType> block;
    private final int size;
    private final int count;
    private final String dimension;

    OreDefinitionImpl(final OreDefinition.Builder.Impl definition, final OreGenerator.Builder builder) {
        this.block = definition.block;
        this.size = definition.size;
        this.count = definition.count;
        this.dimension = builder.string(ContentBuilder.StringType.ID);
    }

    @Override
    public Delegate<BlockType> block() {
        return this.block;
    }

    @Override
    public String dimension() {
        return this.dimension;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public int count() {
        return this.count;
    }

    @Override
    public boolean accepts(final WorldProvider dimension) {
        return this.dimension.equals(dimension.getDimensionType().getName());
    }
}
