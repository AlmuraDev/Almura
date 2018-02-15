/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component;

import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Map;
import java.util.Optional;

public final class DoubleRanges {
    /**
     * A {@link DoubleRange} with a fixed value of {@code 1}.
     */
    public static final DoubleRange FIXED_1 = DoubleRange.fixed(1d);

    private static final ConfigurationNodeDeserializer<DoubleRange> SERIALIZER = node -> {
        if (node.isVirtual()) {
            return Optional.empty();
        }
        if (node.getValue() instanceof Map) {
            return Optional.of(DoubleRange.range(
                    node.getNode("min").getDouble(),
                    node.getNode("max").getDouble()
            ));
        } else if (node.getValue() instanceof Number) {
            return Optional.of(DoubleRange.fixed(node.getDouble()));
        }
        throw new IllegalArgumentException("Unknown type '" + node.getValue() + "'");
    };

    private DoubleRanges() {
    }

    public static Optional<DoubleRange> deserialize(final ConfigurationNode node) {
        return SERIALIZER.deserialize(node);
    }

    public static DoubleRange deserialize(final ConfigurationNode node, final DoubleRange defaultValue) {
        return SERIALIZER.deserialize(node, defaultValue);
    }

    public interface Config {

        String AMOUNT = "amount";
        String BONUS = "bonus";
        String BONUS_AMOUNT = "amount";
        String BONUS_CHANCE = "chance";
    }
}
