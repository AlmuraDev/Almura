/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.GameObject;

public class DropProperty extends VariableGameObjectProperty {

    private final BonusProperty<Integer, Double> bonusProperty;
    private final int data;

    public DropProperty(GameObject object, RangeProperty<Integer> amountProperty, int data, BonusProperty<Integer, Double> bonusProperty) {
        super(object, amountProperty);
        this.data = data;
        this.bonusProperty = bonusProperty;
    }

    public BonusProperty<Integer, Double> getBonusProperty() {
        return bonusProperty;
    }

    public int getData() {
        return data;
    }
}
