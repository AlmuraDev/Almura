/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import java.util.Collections;
import java.util.Set;

public class DelegateSet<V, R> extends DelegateCollection<V, R> implements Set<R> {
    public static final DelegateSet<?, ?> EMPTY = new DelegateSet<>(Object.class, Collections.emptySet());

    public static <V, R> DelegateSet<V, R> empty() {
        return (DelegateSet<V, R>) EMPTY;
    }

    public DelegateSet(final Class<R> real, final Set<Delegate<V>> delegates) {
        super(real, delegates);
    }
}
