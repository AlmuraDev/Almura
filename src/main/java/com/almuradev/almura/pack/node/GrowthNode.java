/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.RangeProperty;

public class GrowthNode implements INode<RangeProperty<Double>> {

    private final RangeProperty<Double> source;

    public GrowthNode(RangeProperty<Double> source) {
        this.source = source;
    }

    @Override
    public RangeProperty<Double> getValue() {
        return source;
    }
}
