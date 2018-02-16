/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

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
        final int amount = this.amount.flooredRandom(random);
        if (this.bonusAmount != null && this.bonusChance != null) {
            final double chance = this.bonusChance.random(random);
            if (random.nextDouble() < (chance / 100)) {
                return amount + this.bonusAmount.flooredRandom(random);
            }
        }
        return amount;
    }
}
