/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.util;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.kyori.lunar.collection.MoreIterables;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import javax.annotation.Nullable;

public final class DoubleRangeFunctionPredicatePair<T> extends AbstractFunctionPredicatePair<T> {
    private final DoubleRange range;

    public DoubleRangeFunctionPredicatePair(@Nullable final FunctionPredicate<T, ResourceLocation> predicate, final DoubleRange range) {
        super(predicate);
        this.range = range;
    }

    public DoubleRange range() {
        return this.range;
    }

    @Nullable
    public static <T> DoubleRange range(final List<DoubleRangeFunctionPredicatePair<T>> predicates, final T biome) {
        for (final DoubleRangeFunctionPredicatePair<T> predicate : predicates) {
            if (predicate.test(biome)) {
                return predicate.range();
            }
        }
        return null;
    }

    public static <T> DoubleRange rangeOrRandom(final List<DoubleRangeFunctionPredicatePair<T>> predicates, final T biome) {
        for (final DoubleRangeFunctionPredicatePair<T> predicate : predicates) {
            if (predicate.test(biome)) {
                return predicate.range();
            }
        }
        return MoreIterables.random(predicates).range();
    }
}
