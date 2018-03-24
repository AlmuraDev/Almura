/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.flower;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.flower.Flower;
import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;

public final class FlowerGeneratorBuilder extends ContentGenerator.Builder.Impl<FlowerGenerator> implements FlowerGenerator.Builder {
    private List<String> worlds = Collections.emptyList();
    private List<DoubleRangeFunctionPredicatePair<Biome>> biomes;
    private List<LazyBlockState> requires = Collections.emptyList();
    private Delegate<Flower> flower;

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
    public void flower(final Delegate<Flower> flower) {
        this.flower = flower;
    }

    @Override
    public FlowerGenerator build() {
        return new FlowerGeneratorImpl(this.worlds, this.biomes, this.requires, this.flower);
    }
}
