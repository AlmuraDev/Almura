/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state.value;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Objects;

final class SimpleStateValue<V extends Comparable<V>> implements StateValue<V> {
    private final String string;

    SimpleStateValue(final String string) {
        this.string = string;
    }

    @Override
    public boolean test(final IProperty<V> property, final IBlockState state) {
        return Objects.equals(this.get(property), state.getValue(property));
    }

    @Override
    public V get(final IProperty<V> property) {
        return property.parseValue(this.string).orNull();
    }
}
