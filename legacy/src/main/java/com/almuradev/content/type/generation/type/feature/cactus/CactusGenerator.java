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
import com.almuradev.content.type.cactus.Cactus;
import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.type.generation.type.feature.FeatureGenerator;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;

public interface CactusGenerator extends FeatureGenerator {

    interface Builder extends ContentGenerator.Builder<CactusGenerator> {

        void worlds(final List<String> worlds);

        void biomes(final List<DoubleRangeFunctionPredicatePair<Biome>> biomes);

        void requires(final List<LazyBlockState> requires);

        void cactus(Delegate<Cactus> cactus);
    }
}
