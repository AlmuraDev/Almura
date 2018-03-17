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
}
