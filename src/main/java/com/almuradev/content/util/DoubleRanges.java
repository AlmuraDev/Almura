/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.util;

import com.almuradev.toolbox.util.math.DoubleRange;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;
import java.util.Random;

public final class DoubleRanges {
    /**
     * A {@link DoubleRange} with a fixed value of {@code 1}.
     */
    public static final DoubleRange FIXED_1 = DoubleRange.fixed(1d);

    private DoubleRanges() {
    }

    public static Optional<DoubleRange> deserialize(final ConfigurationNode node) {
        return DoubleRange.PARSER.deserialize(node);
    }

    public static DoubleRange deserialize(final ConfigurationNode node, final DoubleRange defaultValue) {
        return DoubleRange.PARSER.deserialize(node, defaultValue);
    }

    /**
     * A random value from the whole range, min to max. Use this instead of {@link DoubleRange#random}: the toolbox's version only ever
     * returns values from the upper half of the range (both of its branches map to between the midpoint and max), so a 4-8% chance
     * acted as 6-8%.
     */
    public static double random(final DoubleRange range, final Random random) {
        return range.min() + random.nextDouble() * (range.max() - range.min());
    }

    /**
     * {@link #random} rounded down to a whole number.
     */
    public static int flooredRandom(final DoubleRange range, final Random random) {
        return (int) Math.floor(random(range, random));
    }
}
