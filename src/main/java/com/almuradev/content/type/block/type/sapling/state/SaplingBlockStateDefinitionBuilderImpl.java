/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;

import javax.annotation.Nullable;

public final class SaplingBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<SaplingBlockStateDefinition, SaplingBlockStateDefinitionBuilderImpl> implements SaplingBlockStateDefinitionBuilder {
    Delegate<Tree> tree;
    @Nullable Delegate<Tree> bigTree;
    @Nullable List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances;

    @Override
    public void tree(final Delegate<Tree> tree) {
        this.tree = tree;
    }

    @Override
    public void bigTree(final Delegate<Tree> bigTree, final List<DoubleRangeFunctionPredicatePair<Biome>> chances) {
        this.bigTree = bigTree;
        this.bigTreeChances = chances;
    }

    @Override
    public SaplingBlockStateDefinition build0() {
        return new SaplingBlockStateDefinition(this);
    }
}
