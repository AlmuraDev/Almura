/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import java.util.Set;

public class DelegateSet<V, R> extends DelegateCollection<V, R> implements Set<R> {
    public DelegateSet(final Class<R> real, final Set<Delegate<V>> delegates) {
        super(real, delegates);
    }
}
