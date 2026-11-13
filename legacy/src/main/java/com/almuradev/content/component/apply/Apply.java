/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply;

import com.almuradev.content.component.apply.context.ApplyContext;
import net.minecraft.entity.Entity;

/**
 * Something that be can be applied.
 */
public interface Apply<E extends Entity, C extends ApplyContext> {
    boolean accepts(final Entity entity);

    default void apply(final Entity entity, final C context) {
        if (this.accepts(entity)) {
            this.apply0((E) entity, context);
        }
    }

    void apply0(final E entity, final C context);
}
