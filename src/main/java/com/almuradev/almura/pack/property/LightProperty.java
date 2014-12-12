/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

import com.almuradev.almura.pack.property.source.RangeSource;

public class LightProperty implements IProperty<RangeSource> {

    private final float emission;
    private final int opacity;
    private final RangeSource value;

    public LightProperty(float emission, int opacity, RangeSource value) {
        this.emission = emission;
        this.opacity = opacity;
        this.value = value;
    }

    @Override
    public RangeSource getValue() {
        return value;
    }

    public float getEmission() {
        return emission;
    }

    public int getOpacity() {
        return opacity;
    }
}
