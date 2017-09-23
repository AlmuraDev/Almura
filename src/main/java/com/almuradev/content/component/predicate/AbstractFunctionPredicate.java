/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.predicate;

import java.util.function.Function;

public abstract class AbstractFunctionPredicate<I, O> implements FunctionPredicate<I, O> {

    private final Function<I, O> function;

    protected AbstractFunctionPredicate(final Function<I, O> function) {
        this.function = function;
    }

    @Override
    public final boolean test(final I value) {
        return this.test0(this.function.apply(value));
    }
}
