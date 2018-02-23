/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state;

import com.almuradev.content.type.block.state.value.StateValue;
import com.almuradev.content.type.block.state.value.StateValues;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

enum LazyBlockStateParser implements ConfigurationNodeDeserializer<Map<String, StateValue<? extends Comparable<?>>>> {
    INSTANCE;

    @Override
    public Optional<Map<String, StateValue<? extends Comparable<?>>>> deserialize(final ConfigurationNode config) {
        if (config.isVirtual()) {
            return Optional.empty();
        }
        final Map<String, StateValue<? extends Comparable<?>>> properties = new HashMap<>();
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getChildrenMap().entrySet()) {
            properties.put(String.valueOf(entry.getKey()), StateValues.create(entry.getValue()));
        }
        return Optional.of(properties);
    }
}
