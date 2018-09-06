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
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;

public final class TreeGeneratorBuilder extends ContentGenerator.Builder.Impl<TreeGenerator> implements TreeGenerator.Builder {
    private List<String> worlds = Collections.emptyList();
    private List<DoubleRangeFunctionPredicatePair<Biome>> biomes = Collections.emptyList();
    private List<LazyBlockState> requires = Collections.emptyList();
    private Delegate<Tree> tree;
    private Delegate<Tree> bigTree;
    private List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances;

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
    public void tree(final Delegate<Tree> tree) {
        this.tree = tree;
    }

    @Override
    public void bigTree(final Delegate<Tree> tree, final List<DoubleRangeFunctionPredicatePair<Biome>> chances) {
        this.bigTree = tree;
    }

    @Override
    public TreeGenerator build() {
        return new TreeGeneratorImpl(this.worlds, this.biomes, this.requires, this.tree, this.bigTree, this.bigTreeChances);
    }
}
