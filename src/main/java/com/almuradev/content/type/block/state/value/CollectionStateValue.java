/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state.value;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import net.kyori.lunar.collection.MoreIterables;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class CollectionStateValue<V extends Comparable<V>> implements StateValue<V> {
    private final Function<IProperty<V>, Supplier<Collection<V>>> values;

    CollectionStateValue(final Collection<String> values) {
        final Supplier<Collection<V>>[] supplier = (Supplier<Collection<V>>[]) new Object[1];
        this.values = property -> {
            if (supplier[0] == null) {
                supplier[0] = Suppliers.memoize(() -> values.stream()
                        .map(property::parseValue)
                        .map(Optional::orNull)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));;
            }
            return supplier[0];
        };
    }

    @Override
    public boolean test(final IProperty<V> property, final IBlockState state) {
        return this.values.apply(property).get().contains(state.getValue(property));
    }

    @Nullable
    @Override
    public V get(final IProperty<V> property) {
        return MoreIterables.random(this.values.apply(property).get(), property.getAllowedValues());
    }
}
