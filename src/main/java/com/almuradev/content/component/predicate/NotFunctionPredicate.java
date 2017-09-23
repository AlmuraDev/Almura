/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.predicate;

import java.util.function.Function;

public final class NotFunctionPredicate<I, O> extends AbstractFunctionPredicate<I, O> {

    static final String ID = "not";
    private final FunctionPredicate<I, O> parent;

    public NotFunctionPredicate(final Function<I, O> function, final FunctionPredicate<I, O> parent) {
        super(function);
        this.parent = parent;
    }

    @Override
    public boolean test0(final O value) {
        return !this.parent.test0(value);
    }
}
