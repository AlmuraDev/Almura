/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Map;
import java.util.Optional;

public final class VariableAmounts {

    /**
     * A {@link VariableAmount} with a fixed value of {@code 1}.
     */
    public static final VariableAmount FIXED_1 = VariableAmount.fixed(1d);

    private static final ConfigurationNodeDeserializer<VariableAmount> SERIALIZER = node -> {
        if (node.isVirtual()) {
            return Optional.empty();
        }
        if (node.getValue() instanceof Map) {
            return Optional.of(VariableAmount.range(
                    node.getNode("min").getDouble(),
                    node.getNode("max").getDouble()
            ));
        } else if (node.getValue() instanceof Number) {
            return Optional.of(VariableAmount.fixed(node.getDouble()));
        }
        throw new IllegalArgumentException("Unknown type '" + node.getValue() + "'");
    };

    private VariableAmounts() {
    }

    public static Optional<VariableAmount> deserialize(final ConfigurationNode node) {
        return SERIALIZER.deserialize(node);
    }

    public static VariableAmount deserialize(final ConfigurationNode node, final VariableAmount defaultValue) {
        return SERIALIZER.deserialize(node, defaultValue);
    }

    public interface Config {

        String AMOUNT = "amount";
        String BONUS = "bonus";
        String BONUS_AMOUNT = "amount";
        String BONUS_CHANCE = "chance";
    }
}
