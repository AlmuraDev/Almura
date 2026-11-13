/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import com.google.common.collect.AbstractIterator;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

// Here be voodoo.
public abstract class DelegateCollection<V, R> extends AbstractCollection<R> {
    private final Class<R> real;
    private final Collection<Delegate<V>> delegates;

    DelegateCollection(final Class<R> real, final Collection<Delegate<V>> delegates) {
        this.real = real;
        this.delegates = delegates;
    }

    @Override
    public Iterator<R> iterator() {
        final Iterator<Delegate<V>> it = this.delegates.iterator();
        return new AbstractIterator<R>() {
            @Override
            protected R computeNext() {
                if(it.hasNext()) {
                    return Delegate.require(it.next(), DelegateCollection.this.real);
                }
                return this.endOfData();
            }
        };
    }

    @Override
    public int size() {
        return this.delegates.size();
    }
}
