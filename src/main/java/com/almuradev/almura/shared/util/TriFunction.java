/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

@FunctionalInterface
public interface TriFunction<S, T, U, R> {
    R apply (S s, T t, U u);
}