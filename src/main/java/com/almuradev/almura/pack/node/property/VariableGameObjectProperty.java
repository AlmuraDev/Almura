/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

public class VariableGameObjectProperty extends GameObjectProperty {
    private final RangeProperty<Integer> amountProperty;

    public VariableGameObjectProperty(Object object, RangeProperty<Integer> amount, int data) {
        super(object, data);
        this.amountProperty = amount;
    }

    public RangeProperty<Integer> getAmountProperty() {
        return amountProperty;
    }
}
