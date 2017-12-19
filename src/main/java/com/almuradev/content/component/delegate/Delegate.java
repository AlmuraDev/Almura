/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Delegate<T> extends Predicate<T>, Supplier<T> {

    static <T> Delegate<T> supplying(final Supplier<T> supplier) {
        return new SupplierDelegate<>(supplier);
    }

    default Optional<T> optional() {
        return Optional.ofNullable(this.get());
    }

    @Override
    default boolean test(final T object) {
        return this.get().equals(object);
    }
}
