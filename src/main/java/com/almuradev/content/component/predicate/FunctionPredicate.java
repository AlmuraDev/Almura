/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.predicate;

import java.util.function.Predicate;

public interface FunctionPredicate<I, O> extends Predicate<I> {
    boolean test0(final O value);
}
