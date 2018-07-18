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

import javax.annotation.Nullable;

public interface StateValue<V extends Comparable<V>> {
    boolean test(final IProperty<V> property, final IBlockState state);

    @Nullable
    V get(final IProperty<V> property);
}
