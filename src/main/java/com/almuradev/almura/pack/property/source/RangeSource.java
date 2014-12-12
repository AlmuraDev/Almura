/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property.source;

public class RangeSource implements ISource<Boolean> {

    private final boolean enabled;
    private final double min, max;

    public RangeSource(boolean enabled, double min, double max) {
        this.enabled = enabled;
        this.min = min;
        this.max = max;
    }

    @Override
    public Boolean getSource() {
        return enabled;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
