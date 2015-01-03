/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.mapper.GameObject;

public class VariableGameObjectProperty extends GameObjectProperty {

    private final RangeProperty<Integer> amountProperty;

    public VariableGameObjectProperty(GameObject object, RangeProperty<Integer> amount) {
        super(object);
        this.amountProperty = amount;
    }

    public RangeProperty<Integer> getAmountProperty() {
        return amountProperty;
    }
}
