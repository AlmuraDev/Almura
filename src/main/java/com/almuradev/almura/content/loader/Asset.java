/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.AssetType;
import ninja.leaping.configurate.ConfigurationNode;

import java.nio.file.Path;

public class Asset {

    private final String name;
    private final AssetType assetType;
    private final Path path;
    private final ConfigurationNode node;

    public Asset(String name, AssetType assetType, Path path, ConfigurationNode node) {
        this.name = name;
        this.assetType = assetType;
        this.path = path;
        this.node = node;
    }

    public String getName() {
        return name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public Path getPath() {
        return path;
    }

    public ConfigurationNode getConfigurationNode() {
        return node;
    }
}
