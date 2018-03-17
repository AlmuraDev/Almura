/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.generation.ContentGenerator;
import com.almuradev.content.type.generation.type.feature.FeatureGenerator;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;

public interface TreeGenerator extends FeatureGenerator {
    interface Builder extends ContentGenerator.Builder<TreeGenerator> {
        void worlds(final List<String> worlds);

        void biomes(final List<DoubleRangeFunctionPredicatePair<Biome>> biomes);

        void requires(final List<LazyBlockState> requires);

        void tree(final Delegate<Tree> tree);
    }
}
