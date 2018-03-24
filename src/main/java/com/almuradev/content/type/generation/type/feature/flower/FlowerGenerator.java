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
import com.almuradev.content.type.generation.type.feature.FeatureGenerator;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;

public interface FlowerGenerator extends FeatureGenerator {

    interface Builder extends ContentGenerator.Builder<FlowerGenerator> {

        void worlds(final List<String> worlds);

        void biomes(final List<DoubleRangeFunctionPredicatePair<Biome>> biomes);

        void requires(final List<LazyBlockState> requires);

        void flower(Delegate<Flower> flower);
    }
}
