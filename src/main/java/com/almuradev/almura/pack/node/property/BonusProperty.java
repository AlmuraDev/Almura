/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

public class BonusProperty<T extends Number, U extends Number> extends RangeProperty<T> {

    private final RangeProperty<U> bonusAmountRange;

    public BonusProperty(Class<T> typeClass, boolean enabled, T min, T max, RangeProperty<U> bonusAmountRange) {
        super(typeClass, enabled, min, max);
        this.bonusAmountRange = bonusAmountRange;
    }

    public RangeProperty<U> getBonusAmountRange() {
        return bonusAmountRange;
    }
}
