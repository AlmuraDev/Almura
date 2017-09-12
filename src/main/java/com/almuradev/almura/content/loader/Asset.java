/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.AssetType;
import ninja.leaping.configurate.ConfigurationNode;

import java.nio.file.Path;

public final class Asset {

    private final String name;
    private final AssetType type;
    private final Path path;
    private final ConfigurationNode node;

    Asset(String name, AssetType type, Path path, ConfigurationNode node) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.node = node;
    }

    public String getName() {
        return this.name;
    }

    public AssetType getType() {
        return this.type;
    }

    public Path getPath() {
        return this.path;
    }

    public ConfigurationNode getConfigurationNode() {
        return this.node;
    }
}
