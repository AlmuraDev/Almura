/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.config;

import com.google.common.collect.ObjectArrays;
import ninja.leaping.configurate.ConfigurationNode;

public class ConfigPath {

    private final String[] parts;

    public ConfigPath(final String... parts) {
        this.parts = parts;
    }

    public ConfigPath and(final String... parts) {
        return new ConfigPath(ObjectArrays.concat(this.parts, parts, String.class));
    }

    public ConfigurationNode in(final ConfigurationNode node) {
        return node.getNode((Object[]) this.parts);
    }
}
