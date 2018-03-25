/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.cactus;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.type.cactus.Cactus;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;

public final class CactusGeneratorBuilder extends ContentGenerator.Builder.Impl<CactusGenerator> implements CactusGenerator.Builder {
    private List<String> worlds = Collections.emptyList();
    private List<DoubleRangeFunctionPredicatePair<Biome>> biomes;
    private List<LazyBlockState> requires = Collections.emptyList();
    private Delegate<Cactus> cactus;

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
    public void cactus(final Delegate<Cactus> cactus) {
        this.cactus = cactus;
    }

    @Override
    public CactusGenerator build() {
        return new CactusGeneratorImpl(this.worlds, this.biomes, this.requires, this.cactus);
    }
}
