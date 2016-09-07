/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.property.RangeProperty;

public class LightNode implements INode<RangeProperty<Integer>> {

    private final float emission;
    private final int opacity;
    private final RangeProperty<Integer> value;

    public LightNode(float emission, int opacity, RangeProperty<Integer> value) {
        this.emission = emission;
        this.opacity = opacity;
        this.value = value;
    }

    @Override
    public RangeProperty<Integer> getValue() {
        return value;
    }

    public float getEmission() {
        return emission;
    }

    public int getOpacity() {
        return opacity;
    }
}
