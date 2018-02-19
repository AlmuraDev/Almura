/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.MinimumIntWithVarianceFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public final class TreeBuilder extends ContentBuilder.Impl<Tree> implements Tree.Builder {
    private Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> fruit;
    private Map.Entry<LazyBlockState, List<DoubleRangeFunctionPredicatePair<Biome>>> hanging;
    private List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height;
    private LazyBlockState leaves;
    private LazyBlockState log;

    @Override
    public void fruit(final LazyBlockState block, final List<DoubleRangeFunctionPredicatePair<Biome>> chances) {
        this.fruit = new AbstractMap.SimpleImmutableEntry<>(block, chances);
    }

    @Override
    public void hanging(final LazyBlockState block, final List<DoubleRangeFunctionPredicatePair<Biome>> chances) {
        this.hanging = new AbstractMap.SimpleImmutableEntry<>(block, chances);
    }

    @Override
    public void height(final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height) {
        this.height = height;
    }

    @Override
    public void leaves(final LazyBlockState block) {
        this.leaves = block;
    }

    @Override
    public void log(final LazyBlockState block) {
        this.log = block;
    }

    @Override
    public Tree build() {
        final Tree tree = new TreeFeature(true, this.fruit, this.hanging, this.height, this.leaves, this.log);
        ((IMixinSetCatalogTypeId) tree).setId(this.id, this.name);
        return tree;
    }
}
