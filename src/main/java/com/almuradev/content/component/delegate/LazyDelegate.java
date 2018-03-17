/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import javax.annotation.Nullable;

public abstract class LazyDelegate<T> implements Delegate<T> {
    @Nullable protected T value;

    @Override
    @SuppressWarnings("ConstantConditions")
    public T get() {
        if (this.value != null) {
            return this.value;
        }
        this.value = this.load();
        return this.value;
    }

    @Nullable
    protected abstract T load();
}
