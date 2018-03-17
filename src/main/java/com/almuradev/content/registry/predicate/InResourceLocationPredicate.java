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

import java.util.List;
import java.util.function.Function;

final class InResourceLocationPredicate<T> extends AbstractFunctionPredicate<T, ResourceLocation> {
    static final String ID = "in";
    private final List<String> namespaces;

    InResourceLocationPredicate(final Function<T, ResourceLocation> function, final List<String> namespaces) {
        super(function);
        this.namespaces = namespaces;
    }

    @Override
    public boolean test0(final ResourceLocation value) {
        return this.namespaces.contains(value.getResourceDomain());
    }
}
