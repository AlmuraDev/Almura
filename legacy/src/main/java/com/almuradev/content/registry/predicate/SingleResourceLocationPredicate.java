/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry.predicate;

import com.almuradev.content.component.predicate.AbstractFunctionPredicate;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

final class SingleResourceLocationPredicate<T> extends AbstractFunctionPredicate<T, ResourceLocation> {
    private final ResourceLocation id;

    SingleResourceLocationPredicate(final Function<T, ResourceLocation> function, final ResourceLocation id) {
        super(function);
        this.id = id;
    }

    @Override
    public boolean test0(final ResourceLocation id) {
        return this.id.equals(id);
    }
}
