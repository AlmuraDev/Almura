/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type;

import com.almuradev.almura.configuration.serializer.ConfigurationNodeDeserializer;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Map;
import java.util.Optional;

public final class VariableAmounts {

    public static final VariableAmount FIXED_1 = VariableAmount.fixed(1d);

    public static final ConfigurationNodeDeserializer<VariableAmount> SERIALIZER = node -> {
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
}
