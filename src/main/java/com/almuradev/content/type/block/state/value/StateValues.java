/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state.value;

import com.almuradev.toolbox.util.math.IntRange;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StateValues {
    private static final String TYPE = "type";

    public static <P extends Comparable<P>> StateValue<P> create(final ConfigurationNode config) {
        final Object value = config.getValue();
        if (value instanceof List<?>) {
            return new CollectionStateValue<>(config.getList(Types::asString));
        } else if (value instanceof Map<?, ?>) {
            final Type type = Type.of(config.getNode(TYPE).getString());
            switch (type) {
                case INT_RANGE: return (StateValue<P>) new IntRangeStateValue(IntRange.PARSER.deserialize(config).orElseThrow(() -> new IllegalArgumentException("could not parse int range")));
                default: throw new IllegalStateException("State value type " + type.name() + " was not created");
            }
        }
        return new SimpleStateValue<>(config.getString());
    }

    private enum Type {
        INT_RANGE;

        public static Type of(final String string) {
            switch (string.toLowerCase(Locale.ENGLISH).replace(' ', '_')) {
                case "int_range": return INT_RANGE;
            }
            throw new IllegalArgumentException(string.toLowerCase(Locale.ENGLISH));
        }
    }
}
