/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.ItemProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;

public class GrassNode extends ToggleableNode<ItemProperty> {
    private final ItemProperty itemProperty;
    private final RangeProperty<Double> chanceProperty;

    public GrassNode(boolean isEnabled, ItemProperty itemProperty, RangeProperty<Double> chanceProperty) {
        super(isEnabled);
        this.itemProperty = itemProperty;
        this.chanceProperty = chanceProperty;
    }

    @Override
    public ItemProperty getValue() {
        return itemProperty;
    }

    public RangeProperty<Double> getChanceProperty() {
        return chanceProperty;
    }
}
