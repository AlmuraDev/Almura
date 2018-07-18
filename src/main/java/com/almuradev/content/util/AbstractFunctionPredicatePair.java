/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.util;

import com.almuradev.content.component.predicate.FunctionPredicate;
import net.minecraft.util.ResourceLocation;

import java.util.function.Predicate;

import javax.annotation.Nullable;

public abstract class AbstractFunctionPredicatePair<T> implements Predicate<T> {
    @Nullable private final FunctionPredicate<T, ResourceLocation> predicate;

    protected AbstractFunctionPredicatePair(@Nullable final FunctionPredicate<T, ResourceLocation> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(final T value) {
        return this.predicate == null || this.predicate.test(value);
    }
}
