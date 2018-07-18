/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.AbstractSingleBlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;

import javax.annotation.Nullable;

public final class SaplingBlockStateDefinition extends AbstractSingleBlockStateDefinition<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {
    public final Delegate<Tree> tree;
    @Nullable public final Delegate<Tree> bigTree;
    @Nullable public final List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances;

    SaplingBlockStateDefinition(final SaplingBlockStateDefinitionBuilderImpl builder) {
        super(builder);
        this.tree = builder.tree;
        this.bigTree = builder.bigTree;
        this.bigTreeChances = builder.bigTreeChances;
    }
}
