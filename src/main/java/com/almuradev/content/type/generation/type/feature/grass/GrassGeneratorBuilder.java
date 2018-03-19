/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.grass;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.type.grass.Grass;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;

public final class GrassGeneratorBuilder extends ContentGenerator.Builder.Impl<GrassGenerator> implements GrassGenerator.Builder {
    private List<String> worlds = Collections.emptyList();
    private List<DoubleRangeFunctionPredicatePair<Biome>> biomes;
    private List<LazyBlockState> requires = Collections.emptyList();
    private Delegate<Grass> grass;

    @Override
    public void worlds(final List<String> worlds) {
        this.worlds = worlds;
    }

    @Override
    public void biomes(final List<DoubleRangeFunctionPredicatePair<Biome>> biomes) {
        this.biomes = biomes;
    }

    @Override
    public void requires(final List<LazyBlockState> requires) {
        this.requires = requires;
    }

    @Override
    public void grass(final Delegate<Grass> grass) {
        this.grass = grass;
    }

    @Override
    public GrassGenerator build() {
        return new GrassGeneratorImpl(this.worlds, this.biomes, this.requires, this.grass);
    }
}
