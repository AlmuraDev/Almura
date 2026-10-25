/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.util.math.DoubleRange;

import java.util.Random;

import javax.annotation.Nullable;

public abstract class Drop {

    private final DoubleRange amount;
    @Nullable private final DoubleRange bonusAmount;
    @Nullable private final DoubleRange bonusChance;

    protected Drop(final DoubleRange amount, @Nullable final DoubleRange bonusAmount, @Nullable final DoubleRange bonusChance) {
        this.amount = amount;
        this.bonusAmount = bonusAmount;
        this.bonusChance = bonusChance;
    }

    public int flooredAmount(final Random random) {
        final int amount = wholeAmount(this.amount, random);
        if (this.bonusAmount != null && this.bonusChance != null) {
            final double chance = DoubleRanges.random(this.bonusChance, random);
            if (random.nextDouble() < (chance / 100)) {
                return amount + wholeAmount(this.bonusAmount, random);
            }
        }
        return amount;
    }

    /**
     * A whole amount from the range, every whole number from min to max (inclusive) equally likely: {@code {"min": 24, "max": 32}} drops
     * 24 to 32. {@link DoubleRange#random} only returns values from the upper half of its range, and flooring it would almost never reach
     * max.
     */
    private static int wholeAmount(final DoubleRange range, final Random random) {
        final int min = (int) Math.ceil(range.min()), max = (int) Math.floor(range.max());
        if (max <= min) {
            return (int) Math.floor(range.min());
        }
        return min + random.nextInt(max - min + 1);
    }
}
