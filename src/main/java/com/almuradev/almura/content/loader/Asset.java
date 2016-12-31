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
    private final ConfigurationNode root;

    public Asset(String name, AssetType assetType, Path path, ConfigurationNode root) {
        this.name = name;
        this.assetType = assetType;
        this.path = path;
        this.root = root;
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

    public ConfigurationNode getRoot() {
        return root;
    }
}
