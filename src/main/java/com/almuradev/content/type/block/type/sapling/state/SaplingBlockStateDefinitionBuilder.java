/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;

public interface SaplingBlockStateDefinitionBuilder extends BlockStateDefinition.Builder<SaplingBlockStateDefinition> {
    void tree(final Delegate<Tree> tree);

    void bigTree(final Delegate<Tree> tree, final List<DoubleRangeFunctionPredicatePair<Biome>> chances);
}
