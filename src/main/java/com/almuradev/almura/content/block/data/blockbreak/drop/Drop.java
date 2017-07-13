/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.data.blockbreak.drop;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Random;

import javax.annotation.Nullable;

public abstract class Drop {

    private final VariableAmount amount;
    @Nullable private final VariableAmount bonusAmount;
    @Nullable private final VariableAmount bonusChance;

    Drop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance) {
        this.amount = amount;
        this.bonusAmount = bonusAmount;
        this.bonusChance = bonusChance;
    }

    public int flooredAmount(final Random random) {
        final int amount = this.amount.getFlooredAmount(random);
        if (this.bonusAmount != null && this.bonusChance != null) {
            final double chance = this.bonusChance.getAmount(random);
            if (random.nextDouble() < (chance / 100) ) {
                return amount + this.bonusAmount.getFlooredAmount(random);
            }
        }
        return amount;
    }

    @Override
    public final String toString() {
        return this.toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("amount", this.amount)
                .add("bonusAmount", this.bonusAmount)
                .add("bonusChance", this.bonusChance);
    }
}
