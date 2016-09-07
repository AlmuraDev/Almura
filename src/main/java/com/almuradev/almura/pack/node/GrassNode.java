/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.node.property.VariableGameObjectProperty;

public class GrassNode extends ToggleableNode<VariableGameObjectProperty> {

    private final VariableGameObjectProperty variableGameObjectProperty;
    private final RangeProperty<Double> chanceProperty;

    public GrassNode(boolean isEnabled, VariableGameObjectProperty variableGameObjectProperty, RangeProperty<Double> chanceProperty) {
        super(isEnabled);
        this.variableGameObjectProperty = variableGameObjectProperty;
        this.chanceProperty = chanceProperty;
    }

    @Override
    public VariableGameObjectProperty getValue() {
        return variableGameObjectProperty;
    }

    public RangeProperty<Double> getChanceProperty() {
        return chanceProperty;
    }
}
