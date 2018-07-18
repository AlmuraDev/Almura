/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.util;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.toolbox.util.math.IntRange;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

import javax.annotation.Nullable;

public final class MinimumIntWithVarianceFunctionPredicatePair<T> extends AbstractFunctionPredicatePair<T> {
    private final int min;
    private final IntRange variance;

    public MinimumIntWithVarianceFunctionPredicatePair(@Nullable final FunctionPredicate<T, ResourceLocation> predicate, final int min, final IntRange variance) {
        super(predicate);
        this.min = min;
        this.variance = variance;
    }

    public int get(final Random random) {
        return this.min + this.variance.random(random);
    }
}
