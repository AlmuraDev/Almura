/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.deadbush;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.deadbush.DeadBush;
import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;

public final class DeadBushGeneratorBuilder extends ContentGenerator.Builder.Impl<DeadBushGenerator> implements DeadBushGenerator.Builder {
    private List<String> worlds = Collections.emptyList();
    private List<DoubleRangeFunctionPredicatePair<Biome>> biomes;
    private List<LazyBlockState> requires = Collections.emptyList();
    private Delegate<DeadBush> deadBush;

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
    public void deadBush(final Delegate<DeadBush> deadBush) {
        this.deadBush = deadBush;
    }

    @Override
    public DeadBushGenerator build() {
        return new DeadBushGeneratorImpl(this.worlds, this.biomes, this.requires, this.deadBush);
    }
}
