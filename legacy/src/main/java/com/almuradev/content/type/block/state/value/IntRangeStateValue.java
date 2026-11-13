/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state.value;

import com.almuradev.toolbox.util.math.IntRange;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.concurrent.ThreadLocalRandom;

final class IntRangeStateValue implements RangeStateValue<Integer> {
    private final IntRange range;

    IntRangeStateValue(final IntRange range) {
        this.range = range;
    }

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Integer min() {
        return this.range.min();
    }

    @Override
    public Integer max() {
        return this.range.max();
    }

    @Override
    public boolean test(final IProperty<Integer> property, final IBlockState state) {
        return this.range.contains(state.getValue(property));
    }

    @Override
    public Integer get(final IProperty<Integer> property) {
        return this.range.random(ThreadLocalRandom.current());
    }
}
