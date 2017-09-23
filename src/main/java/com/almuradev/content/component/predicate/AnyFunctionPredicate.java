/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.predicate;

import java.util.List;
import java.util.function.Function;

public final class AnyFunctionPredicate<I, O> extends AbstractMultiFunctionPredicate<I, O> {

    static final String ID = "any";

    public AnyFunctionPredicate(final Function<I, O> function, final List<FunctionPredicate<I, O>> predicates) {
        super(function, predicates);
    }

    @Override
    public boolean test0(final O value) {
        for (final FunctionPredicate<I, O> predicate : this.predicates) {
            if (predicate.test0(value)) {
                return true;
            }
        }
        return false;
    }
}
