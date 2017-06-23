/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.serializer;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;

public interface ConfigurationNodeDeserializer<T> {

    default T requiredDeserialize(final ConfigurationNode node, final String string) {
        return this.deserialize(node).orElseThrow(() -> new IllegalArgumentException(string));
    }

    default T defaultedDeserialize(final ConfigurationNode node, final T defaultValue) {
        return this.deserialize(node).orElse(defaultValue);
    }

    Optional<T> deserialize(final ConfigurationNode node);
}
