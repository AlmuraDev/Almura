/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class SupplierDelegate<T> extends LazyDelegate<T> {

    private final Supplier<T> supplier;

    SupplierDelegate(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Nullable
    @Override
    protected T load() {
        return this.supplier.get();
    }
}
