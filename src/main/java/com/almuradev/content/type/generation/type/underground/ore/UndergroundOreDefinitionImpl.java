/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore;

import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.block.state.LazyBlockState;
import net.minecraft.world.World;

import java.util.List;

final class UndergroundOreDefinitionImpl implements UndergroundOreDefinition {
    private final LazyBlockState block;
    private final int size;
    private final int count;
    private final List<String> worlds;
    private final String dimension;

    UndergroundOreDefinitionImpl(final UndergroundOreDefinition.Builder.Impl definition, final UndergroundOreGenerator.Builder builder) {
        this.block = definition.block;
        this.size = definition.size;
        this.count = definition.count;
        this.worlds = definition.worlds;
        this.dimension = builder.string(ContentBuilder.StringType.NAME);
    }

    @Override
    public LazyBlockState block() {
        return this.block;
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
    public boolean accepts(final World world) {
        if (!this.dimension.equals(world.provider.getDimensionType().getName())) {
            return false;
        }
        if (this.worlds.isEmpty()) {
            return true;
        } else {
            for (final String name : this.worlds) {
                if (((org.spongepowered.api.world.World) world).getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }
    }
}
