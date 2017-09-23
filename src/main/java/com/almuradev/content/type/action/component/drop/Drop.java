/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Random;

import javax.annotation.Nullable;

public abstract class Drop {

    private final VariableAmount amount;
    @Nullable private final VariableAmount bonusAmount;
    @Nullable private final VariableAmount bonusChance;

    protected Drop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance) {
        this.amount = amount;
        this.bonusAmount = bonusAmount;
        this.bonusChance = bonusChance;
    }

    public int flooredAmount(final Random random) {
        final int amount = this.amount.getFlooredAmount(random);
        if (this.bonusAmount != null && this.bonusChance != null) {
            final double chance = this.bonusChance.getAmount(random);
            if (random.nextDouble() < (chance / 100)) {
                return amount + this.bonusAmount.getFlooredAmount(random);
            }
        }
        return amount;
    }
}
