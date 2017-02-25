/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

public class BonusProperty<T extends Number, U extends Number> extends RangeProperty<T> {

    private final RangeProperty<U> bonusChanceRange;

    public BonusProperty(Class<T> typeClass, boolean enabled, RangeProperty<T> bonusAmountRange, RangeProperty<U> bonusChanceRange) {
        this(typeClass, enabled, bonusAmountRange.getMin(), bonusAmountRange.getMax(), bonusChanceRange);
    }

    public BonusProperty(Class<T> typeClass, boolean enabled, T min, T max, RangeProperty<U> bonusChanceRange) {
        super(typeClass, enabled, min, max);
        this.bonusChanceRange = bonusChanceRange;
    }

    public RangeProperty<U> getBonusChanceRange() {
        return bonusChanceRange;
    }
}
