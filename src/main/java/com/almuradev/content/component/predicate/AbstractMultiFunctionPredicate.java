/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.predicate;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractMultiFunctionPredicate<I, O> extends AbstractFunctionPredicate<I, O> {

    final List<FunctionPredicate<I, O>> predicates;

    protected AbstractMultiFunctionPredicate(final Function<I, O> function, final List<FunctionPredicate<I, O>> predicates) {
        super(function);
        this.predicates = filterNull(predicates);
    }

    private static <I, O> List<FunctionPredicate<I, O>> filterNull(final List<FunctionPredicate<I, O>> list) {
        list.removeAll(Collections.singleton(null));
        return list;
    }
}
