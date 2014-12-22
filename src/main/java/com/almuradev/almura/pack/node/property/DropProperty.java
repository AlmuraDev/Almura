/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

public class DropProperty extends VariableGameObjectProperty {

    private final BonusProperty<Integer, Double> bonusProperty;

    public DropProperty(Object object, RangeProperty<Integer> amountProperty, int data, BonusProperty<Integer, Double> bonusProperty) {
        super(object, amountProperty, data);
        this.bonusProperty = bonusProperty;
    }

    public BonusProperty<Integer, Double> getBonusProperty() {
        return bonusProperty;
    }
}
