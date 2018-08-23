/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.filter;

@FunctionalInterface
public interface Filter<T> {

    boolean test(final T target, final String value);
}
