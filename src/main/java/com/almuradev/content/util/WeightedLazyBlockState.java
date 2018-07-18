/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.util;

import com.almuradev.content.type.block.state.LazyBlockState;
import net.minecraft.util.WeightedRandom;

public final class WeightedLazyBlockState extends WeightedRandom.Item {

    private final LazyBlockState lazyBlockState;

    public WeightedLazyBlockState(int itemWeightIn, LazyBlockState lazyBlockState) {
        super(itemWeightIn);
        this.lazyBlockState = lazyBlockState;
    }

    public LazyBlockState getLazyBlockState() {
        return this.lazyBlockState;
    }
}
