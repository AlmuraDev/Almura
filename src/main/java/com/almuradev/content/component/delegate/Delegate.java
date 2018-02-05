/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public interface Delegate<T> extends Predicate<T>, Supplier<T> {

    static <T> Delegate<T> supplying(final Supplier<T> supplier) {
        return new SupplierDelegate<>(supplier);
    }

    @Nullable
    static <T> T get(@Nullable final Delegate<T> delegate) {
        return delegate == null ? null : delegate.get();
    }

    static <T> T require(@Nullable final Delegate<T> delegate) {
        return Objects.requireNonNull(get(delegate), () -> String.valueOf(delegate));
    }

    @Nullable
    static <T, R> R get(@Nullable final Delegate<T> delegate, final Class<R> real) {
        return delegate == null ? null : real.cast(delegate.get());
    }

    static <T, R> R require(@Nullable final Delegate<T> delegate, final Class<R> real) {
        return Objects.requireNonNull(get(delegate, real), () -> String.valueOf(delegate));
    }

    static <T> Optional<T> optional(@Nullable final Delegate<T> delegate) {
        return delegate == null ? Optional.empty() : delegate.optional();
    }

    default Optional<T> optional() {
        return Optional.ofNullable(this.get());
    }

    @Override
    default boolean test(final T object) {
        return this.get().equals(object);
    }
}
