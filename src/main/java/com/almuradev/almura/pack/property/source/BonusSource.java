/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property.source;

public class BonusSource extends RangeSource {

    private final int minAmount, maxAmount;

    public BonusSource(boolean enabled, double minChance, double maxChance, int minAmount, int maxAmount) {
        super(enabled, minChance, maxChance);
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }
}
