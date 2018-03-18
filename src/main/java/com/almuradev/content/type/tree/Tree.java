/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.content.util.MinimumIntWithVarianceFunctionPredicatePair;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.util.PEBKACException;

import java.util.List;

public interface Tree extends CatalogedContent {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    interface Builder extends ContentBuilder<Tree> {
        void big(final boolean big);

        void fruit(final LazyBlockState block, final List<DoubleRangeFunctionPredicatePair<Biome>> chances);

        void hanging(final LazyBlockState block, final List<DoubleRangeFunctionPredicatePair<Biome>> chances);

        void height(final List<MinimumIntWithVarianceFunctionPredicatePair<Biome>> height);

        void leaves(final LazyBlockState block);

        void log(final LazyBlockState block);
    }
}
