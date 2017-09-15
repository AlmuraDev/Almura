/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.config;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;

@FunctionalInterface
public interface ConfigurationNodeDeserializer<T> {

    Optional<T> deserialize(final ConfigurationNode node);

    default T deserialize(final ConfigurationNode node, final T defaultValue) {
        return this.deserialize(node).orElse(defaultValue);
    }
}
