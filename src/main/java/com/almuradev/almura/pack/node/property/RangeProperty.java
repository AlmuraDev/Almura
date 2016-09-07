/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;

public class RangeProperty<T extends Number> implements IProperty<Boolean> {

    public static final Random RANDOM = new Random();
    private final Class<T> typeClass;
    private final boolean enabled;
    private final T min, max;

    public RangeProperty(Class<T> typeClass, boolean enabled, T min, T max) {
        this.typeClass = typeClass;
        this.enabled = enabled;
        this.max = max;
        this.min = min;
    }

    public RangeProperty(Class<T> typeClass, boolean enabled, Pair<T, T> range) {
        this.typeClass = typeClass;
        this.enabled = enabled;
        min = range.getLeft();
        max = range.getRight();
    }

    @Override
    public Boolean getSource() {
        return enabled;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    @SuppressWarnings("unchecked")
    public T getValueWithinRange() {
        if (typeClass == Float.class) {
            return (T) new Float(min.floatValue() + (max.floatValue() - min.floatValue()) * RANDOM.nextFloat());
        } else if (typeClass == Double.class) {
            return (T) new Double(Math.random() < 0.5 ? ((1 - Math.random()) * (max.doubleValue() - min.doubleValue()) + min.doubleValue())
                    : (Math.random() * (max.doubleValue() - min.doubleValue()) + min.doubleValue()));
        } else if (typeClass == Integer.class) {
            return (T) new Integer(RANDOM.nextInt((max.intValue() - min.intValue()) + 1) + min.intValue());
        } else {
            return null;
        }
    }
}
