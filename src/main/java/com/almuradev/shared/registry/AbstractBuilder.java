/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.registry;

import org.spongepowered.api.util.ResettableBuilder;

public interface AbstractBuilder<T, B extends AbstractBuilder<T, B>> extends ResettableBuilder<T, B> {

    @Override
    default B from(final T value) {
        return (B) this;
    }
}
